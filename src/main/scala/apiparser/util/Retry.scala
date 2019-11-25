package apiparser.util

object Retry {
  /**
   *
   * @param n: retry number
   * @param interval: interval in sec
   * @param fn
   * @tparam T
   * @return
   */
  def retry[T](n: Int, interval: Int)(fn: => T): T = {
    try{
      fn
    } catch {
      case e =>
        if(n > 1){
          Thread.sleep(interval * 1000)
          retry(n - 1, interval)(fn)
        } else throw e
    }
  }
}
