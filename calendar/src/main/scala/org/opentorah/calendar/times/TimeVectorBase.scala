package org.opentorah.calendar.times

import org.opentorah.calendar.numbers.VectorNumber

trait TimeVectorBase[S <: Times[S]]
  extends VectorNumber[S] with Time[S, S#Vector]
{ this: S#Vector =>
}