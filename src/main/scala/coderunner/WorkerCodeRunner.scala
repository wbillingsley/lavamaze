package coderunner

import coderunner.CodeRunner.RemoteParty
import org.scalajs.dom.MessageEvent
import org.scalajs.dom.raw.{Blob, BlobPropertyBag, URL}
import org.scalajs.dom.webworkers.Worker

import scala.concurrent.{Future, Promise}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.scalajs.js

object WorkerCodeRunner{

  /**
   * Script that is loaded on the worker side, allowing us to send it code to execute
   * (that includes sending messages back).
   */
  val commonScript:String =
    s"""
       |/*
       |  Messages are expected to be in this format:
       |
       |  {
       |      key: String,
       |      kind: String,   // value, functionAndBinginds
       |      payload: Any
       |  }
       |
       | */
       |
       |/** RPCs back to the main page that have been made and are pending a return or error reply */
       |let pending = {}
       |
       |/** Keys used in the pending dictionary */
       |let lastKey = BigInt(0)
       |
       |/** Get a key for a message. These increment so they are locally unique. */
       |function getKey() {
       |    lastKey = lastKey + BigInt(1)
       |    return lastKey
       |}
       |
       |/** Creates a function that will make an rpc call to the given name */
       |function remotify(name) {
       |    return async function() {
       |      // extract from Arguments object into an array. (postMessage can't clone an Arguments object to send it.)
       |      let argV = [...arguments]
       |
       |      // compose an rpc call. "this" in the rpc is always {}
       |      return rpc(["args"], [argV], "return " + name + ".apply({}, args)")
       |    }
       |}
       |
       |function messageHandler(e) {
       |    console.debug("worker received ", e.data)
       |    let message = e.data
       |
       |    if (message.kind == "call") {
       |        /*
       |           Calls look like:
       |
       |           {
       |             key: "Some key",                           // the sender's key to track when a return value is sent
       |             kind: "call",
       |             payload: {
       |               argNames: ["i", "j", "k"],               // arguments to have in scope
       |               argValues: [1, 2, 3],                    // values for those arguments
       |               rpcNames: ["walkForward", "turnAround"], // calls the script can make back to the main page
       |               code: `
       |                 console.log("j was " + j);
       |                 walkForward(2); turnAround(); walkForward(3);
       |               `
       |             }
       |           }
       |        */
       |        let AsyncFunction = (async function(){}).constructor
       |        let localArgNames = message.payload.argNames
       |        let localArgVals = message.payload.argValues
       |        let rpcNames = message.payload.rpcs
       |
       |        // Automatically create rpc functions for the rpc calls
       |        let rpcVals = rpcNames.map(remotify)
       |
       |        try {
       |          let f = AsyncFunction(...rpcNames, ...localArgNames, message.payload.code)
       |          let bound = f.bind(null, ...rpcVals, ...localArgVals)
       |          let run = bound()
       |
       |          run.then((r) => {
       |            target.postMessage({
       |                key: message.key,
       |                kind: "return",
       |                payload: r
       |            })
       |          }, (err) => {
       |            target.postMessage({
       |                key: message.key,
       |                kind: "error",
       |                payload: err
       |            })
       |          })
       |        } catch (err) {
       |          target.postMessage({
       |                key: message.key,
       |                kind: "error",
       |                payload: err
       |          })
       |        }
       |
       |    } else if (message.kind == "return") {
       |        let promise = pending[message.key]
       |        delete pending[message.key]
       |        promise.resolve(message.payload)
       |    } else if (message.kind == "error") {
       |        let promise = pending[message.key]
       |        delete pending[message.key]
       |        promise.reject(message.payload)
       |    } else {
       |        console.error("Received message didn't match any known kind", message)
       |    }
       |}
       |
       |/** Asks the main page to run code for the worker */
       |async function rpc(argNames, argValues, code) {
       |    let key = getKey()
       |    let message = {
       |        key: key,
       |        kind: "call",
       |        payload: {
       |            argNames: argNames,
       |            argValues: argValues,
       |            code: code
       |        }
       |    }
       |
       |    let promise = new Promise(function (resolve, reject) {
       |        pending[message.key] = {
       |            resolve: (v) => {
       |              console.debug("resolving", v)
       |              resolve(v)
       |            },
       |            reject: (e) => {
       |              console.debug("rejecting", e)
       |              reject(e)
       |            }
       |        }
       |    })
       |
       |    console.debug("Sending rpc to main", message)
       |    target.postMessage(message)
       |    return promise
       |}
       |
       |// Tell the main page we're ready to receive code to run
       |target.postMessage({
       |  kind: "loaded"
       |})
       |""".stripMargin

  protected val workerScript:String =
    s"""
      |// Web Workers call self.postMessage to send messages to the main page
      |let target = self
      |
      |$commonScript
      |
      |onmessage = messageHandler
      |
      |""".stripMargin

  /** Load the script into a blob so we can give it a URL */
  private val workerScriptBlob = new Blob(js.Array(workerScript), BlobPropertyBag(`type` = "application/javascript"))

  /** Give the script blob a URL so that created workers can load it */
  val workerScriptBlobURL:String = URL.createObjectURL(workerScriptBlob)

  class WebWorkerRemote(w:Worker) extends RemoteParty {
    override def setMessageHandler(f: Function[MessageEvent, _]): Unit = {
      w.onmessage = f
    }

    override def postMessage(message: js.Any): Unit = {
      w.postMessage(message)
    }

    override def terminate(): Unit = {
      w.terminate()
    }
  }

}

/**
 * A code runner that creates a WebWorker as the Remote Party to execute the script
 */
class WorkerCodeRunner(
  rpcs:Map[String, js.Function], bindings:Map[String, js.Any],
  awaitifyRpcs:Boolean = false
) extends CodeRunner(rpcs, bindings, awaitifyRpcs) {

  override protected def createWorker(): Future[CodeRunner.RemoteParty] = {
    val w = new Worker(WorkerCodeRunner.workerScriptBlobURL)
    val party = new WorkerCodeRunner.WebWorkerRemote(w)
    val p = Promise[CodeRunner.RemoteParty]()
    party.setMessageHandler(makeMessageHandler(
      onLoaded = () => { p.success(party) },
      destination = party
    ))

    p.future
  }

  override def terminateWorker():Unit = {
    for {
      f <- worker
      w <- f
    } w.terminate()
  }

}