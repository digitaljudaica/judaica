package org.opentorah.util

// TODO generalize Parser to take additional state and pass Cache in instead of using global one!
abstract class Cache[S, T] extends Function[S, T] {
  private val values = new collection.mutable.WeakHashMap[S, T]

  final def get(s: S): T = values.getOrElseUpdate(s, calculate(s))

  final override def apply(s: S): T = get(s)

  final def get(s: S, useCache: Boolean): T = if (useCache) get(s) else calculate(s)

  def calculate(s: S): T
}
