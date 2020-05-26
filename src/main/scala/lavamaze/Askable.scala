package lavamaze

/**
 * A miniature version of the Actor model's ask pattern
 */
trait Askable[-T, +R] {

  /** Send a message, with a callback for the reply */
  def ask(message:T, receive: R => Any)

}
