package coderunner

import com.wbillingsley.veautiful.logging.Logger
import org.scalajs.dom
import org.scalajs.dom.MessageEvent

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Future, Promise}
import scala.scalajs.js
import scala.scalajs.js.JSConverters._

object CodeRunner {

  val asyncConstructor:js.Dynamic = {
    js.eval("Object.getPrototypeOf(async function(){}).constructor").asInstanceOf[js.Dynamic]
  }

  /**
   * Takes a block of code and calls it asynchronously, by binding it into an AsyncFunction
   * @param thisVal what "this" should be in the script
   * @param args name, value pairs that you want in scope in the code block
   * @param await strings that should have "await " put in front of them. This can be useful if you have code that you
   *              want to write as if it is synchronous (e.g. gameCharacter.walkForward(3) ) but actually implement
   *              asynchronously so that the draw loop can still operate while the character is walking.
   * @param code the code to execute
   * @return
   */
  def asyncCall(thisVal: js.Any = new js.Object(), args:Seq[(String, js.Any)] = Seq.empty, await:Seq[String] = Seq.empty, code:String = ""):js.Dynamic = {
    asyncBind(thisVal, args, await, code)()
  }

  /**
   * Takes a block of code and inserts "await " before calls to specified functions.
   */
  def awaitify(code:String, await:Iterable[String]):String = {
    await.fold(code) { (s, term) => s.replaceAll(term, "await " + term)}
  }

  /**
   * Takes a block of code and composes it into a bound AsyncFunction
   * @param thisVal what "this" should be in the script
   * @param args name, value pairs that you want in scope in the code block
   * @param await strings that should have "await " put in front of them. This can be useful if you have code that you
   *              want to write as if it is synchronous (e.g. gameCharacter.walkForward(3) ) but actually implement
   *              asynchronously so that the draw loop can still operate while the character is walking.
   * @param code the code to execute
   * @return
   */
  def asyncBind(thisVal: js.Any = new js.Object(), args:Seq[(String, js.Any)] = Seq.empty, await:Seq[String] = Seq.empty, code:String = ""):js.Dynamic = {
    // we have to add the js.Any type annotation or we get a complaint about required Seq[js.Any] found Seq[String]
    val argNames = args.map(x => x._1:js.Any)

    // Well isn't this filthy... we run through the string inserting await before the terms we want to await on
    val modifiedCode:js.Any = if (await.isEmpty) code else awaitify(code, await)

    // this needs to be a js.Function, or func.bind below will complain about not supporting varargs
    val func = asyncConstructor.apply(argNames :+ modifiedCode :_*).asInstanceOf[js.Function]

    // we have to add the js.Any type annotation or we get a complaint about required Seq[js.Any] found Seq[String]
    val argVals = args.map(_._2:js.Any)

    func.bind(thisVal, argVals:_*)
  }

  /**
   * Takes a block of code and runs it inside a synchronous function
   * @param thisVal what "this" should be in the code block
   * @param args name, value pairs you want in scope in the code block
   * @param code the code to run
   * @return
   */
  def syncCall(thisVal: js.Any = new js.Object(), args:Seq[(String, js.Any)] = Seq.empty, code:String = ""):js.Dynamic = {
    syncBind(thisVal, args, code)()
  }

  /**
   * Takes a block of code and binds it inside a synchronous function, ready to run but not yet run
   * @param thisVal what "this" should be in the code block
   * @param args name, value pairs you want in scope in the code block
   * @param code the code to run
   * @return
   */
  def syncBind(thisVal: js.Any = new js.Object(), args:Seq[(String, js.Any)] = Seq.empty, code:String = ""):js.Dynamic = {
    val argNames = args.map(_._1)
    val func = new js.Function(argNames :+ code :_*)
    val argVals = args.map(_._2)
    func.bind(thisVal, argVals:_*)
  }



  val logger:Logger = Logger.getLogger(CodeRunner.getClass)

  /** Something we can send a message to */
  trait RemoteParty {
    def setMessageHandler(f: MessageEvent => _):Unit
    def postMessage(message:js.Any):Unit
    def terminate():Unit
  }

}

abstract class CodeRunner(rpcs:Map[String, js.Function], bindings:Map[String, js.Any], awaitifyRpcs:Boolean) {

  private val replyMap = mutable.Map.empty[String, Promise[js.Any]]
  private var lastKey = BigInt(0)

  private def nextKey():String = {
    lastKey += 1
    lastKey.toString
  }

  private def logger = CodeRunner.logger

  /**
   * Generates a function, for the remote worker, that performs an rpc instead of a call.
   */
  private def remotify(name:String):js.Function = {
    js.Function.apply("", s"rpc(['args'], [arguments], $name.apply(args))")
  }

  /**
   * The web worker that will run the user-written code
   */
  protected var worker:Option[Future[CodeRunner.RemoteParty]] = None


  /**
   * Creates a message handler for the given messagable
   * @param onLoaded what to do when the other party has loaded
   * @param destination where to send messages
   * @return
   */
  protected def makeMessageHandler(
    onLoaded: () => Unit,
    destination: CodeRunner.RemoteParty
  ): MessageEvent => Unit = { e =>
    logger.debug("Received " + e.data)
    val message = e.data.asInstanceOf[js.Dynamic]
    message.kind.toString match {
      case "loaded" =>
        logger.debug("worker loaded")
        onLoaded()
      case "call" =>
        val argNames: mutable.Seq[String] = message.payload.argNames.asInstanceOf[js.Array[String]]
        val argValues: mutable.Seq[js.Any] = message.payload.argValues.asInstanceOf[js.Array[js.Any]]
        val localBindings = argNames.zip(argValues) ++ rpcs

        val code = message.payload.code.toString

        val func = CodeRunner.asyncBind(args = localBindings.toSeq, code = code)
        val result = func()

        result.`then`({ r: js.Any =>
          destination.postMessage(js.Dictionary(
            "key" -> message.key,
            "kind" -> "return",
            "payload" -> r
          ))
        }, { err: js.Any =>
          destination.postMessage(js.Dictionary(
            "key" -> message.key,
            "kind" -> "error",
            "payload" -> err
          ))
        })

      case "return" =>
        logger.debug("reply was " + message.payload)
        val key = message.key.toString
        for {prom <- replyMap.get(key)} {
          replyMap.remove(key)
          prom.success(message.payload)
        }

      case "error" =>
        logger.debug("error was " + message.payload)
        val key = message.key.toString
        for {prom <- replyMap.get(key)} {
          replyMap.remove(key)
          prom.failure(js.JavaScriptException(message.payload))
        }

    }
  }

  protected def createWorker(): Future[CodeRunner.RemoteParty]

  protected def terminateWorker():Unit

  def reset():Unit = {
    terminateWorker()
    worker = None
  }

  def remoteExecute(code:String):Future[js.Any] = {

    if (worker.isEmpty) worker = Some(createWorker())

    val argNames = bindings.map(_._1:js.Any)
    val argValues = bindings.map(b => remotify(b._1))
    val rpcNames = rpcs.map({ case (name, _) => name })

    val key = nextKey()

    val message = js.Dictionary[js.Any](
      "kind" -> "call",
      "key" -> key,
      "payload" -> js.Dictionary[js.Any](
        "argNames" -> argNames.toJSArray,
        "argValues" -> argValues.toJSArray,
        "rpcs" -> rpcNames.toJSArray,
        "code" -> (if (awaitifyRpcs) CodeRunner.awaitify(code, rpcNames) else code)
      )
    )

    val p = Promise[js.Any]
    replyMap(key) = p
    for {
      f <- worker
      w <- f
    } {
      dom.console.debug("Sending ", message)
      w.postMessage(message)
    }

    p.future
  }

}




