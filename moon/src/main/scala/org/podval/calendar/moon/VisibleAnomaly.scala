/*
 * Copyright 2011 Podval Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.podval.calendar.moon

import scala.collection.immutable.Map


object VisibleAnomaly {

/*
   120  4 20'       (4 40')
   150  3 48'       (2 48')
   170  1 59'         (59')
*/

    /* Numbers as they are printed in Chapter 15 Law 6 */
    // TODO sort by maslul
    val MISPRINTED = Map(
        Angle( 10) -> Angle(0, 50),
        Angle( 20) -> Angle(1, 38),
        Angle( 30) -> Angle(2, 24),
        Angle( 40) -> Angle(3,  6),
        Angle( 50) -> Angle(3, 44),
        Angle( 60) -> Angle(4, 16),
        Angle( 70) -> Angle(4, 41),
        Angle( 80) -> Angle(5,  0),
        Angle( 90) -> Angle(5,  5),
        Angle(100) -> Angle(5,  8),
        Angle(110) -> Angle(4, 59),
        Angle(120) -> Angle(4, 20),
        Angle(130) -> Angle(4, 14),
        Angle(140) -> Angle(3, 33),
        Angle(150) -> Angle(3, 48),
        Angle(160) -> Angle(1, 56),
        Angle(170) -> Angle(1, 59)
    )


    val CORRECT = MISPRINTED ++ Map(
        Angle(120) -> Angle(4, 40),
        Angle(150) -> Angle(2, 48),
        Angle(170) -> Angle(0, 59)
    )


    def mnasfrome(maslul: Angle, e: Double): Angle =
        Angle.asin(maslul.sin()/scala.math.sqrt(e*e + 2*e*maslul.cos() + 1), 1)


    def efrommnas(maslul: Angle, mnas: Angle): Double =
        maslul.sin()/mnas.sin()*scala.math.abs(mnas.cos())-maslul.cos()


    def efrommnasround(maslul: Angle, mnas: Angle): Double = round(efrommnas(maslul, mnas), 2)


    private def round(value: Double, digits: Int): Double = {
        val quotient = scala.math.pow(10, digits)
        scala.math.round(value*quotient)/quotient
    }
}
