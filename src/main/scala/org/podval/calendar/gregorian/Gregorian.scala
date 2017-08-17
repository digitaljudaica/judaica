package org.podval.calendar.gregorian

import org.podval.calendar.calendar._

class Gregorian private() extends Calendar[Gregorian] {

  trait GregorianCalendarMember extends CalendarMember[Gregorian] {
    final override def calendar: Gregorian = Gregorian.this
  }

  abstract class GregorianYear(number: Int) extends YearBase(number) { this: Year =>
    final override def firstDayNumber: Int = Year.firstDay(number)

    final override def lengthInDays: Int = Year.lengthInDays(number)

    final override def character: YearCharacter = isLeap
  }

  final override type Year = GregorianYear

  final override def createYear(number: Int): Year =
    new GregorianYear(number) with GregorianCalendarMember

  final override type YearCharacter = Boolean

  final override val Year: GregorianYearCompanion = new GregorianYearCompanion with GregorianCalendarMember

  abstract class GregorianYearCompanion extends YearCompanion {
    protected final override def characters: Seq[YearCharacter] = Seq(true, false)

    protected final override def monthNamesAndLengths(isLeap: YearCharacter): List[MonthNameAndLength] = {
      import MonthName._
      List(
        MonthNameAndLength(January  , 31),
        MonthNameAndLength(February , if (isLeap) 29 else 28),
        MonthNameAndLength(March    , 31),
        MonthNameAndLength(April    , 30),
        MonthNameAndLength(May      , 31),
        MonthNameAndLength(June     , 30),
        MonthNameAndLength(July     , 31),
        MonthNameAndLength(August   , 31),
        MonthNameAndLength(September, 30),
        MonthNameAndLength(October  , 31),
        MonthNameAndLength(November , 30),
        MonthNameAndLength(December , 31)
      )
    }

    protected final override def areYearsPositive: Boolean = false

    final override def isLeap(yearNumber: Int): Boolean =
      (yearNumber % 4 == 0) && ((yearNumber % 100 != 0) || (yearNumber % 400 == 0))

    final override def firstMonth(yearNumber: Int): Int =
      monthsInYear*(yearNumber - 1) + 1

    final override def lengthInMonths(yearNumber: Int): Int = monthsInYear

    val monthsInYear: Int = 12

    private val daysInNonLeapYear: Int = 365

    def firstDay(yearNumber: Int): Int =
      daysInNonLeapYear * (yearNumber - 1) + (yearNumber - 1)/4 - (yearNumber - 1)/100 +
        (yearNumber - 1)/400 + 1

    def lengthInDays(yearNumber: Int): Int =
      if (Year.isLeap(yearNumber)) daysInNonLeapYear + 1 else daysInNonLeapYear
  }


  final override type Month = GregorianMonth

  final override def createMonth(number: Int): Month =
    new GregorianMonth(number) with GregorianCalendarMember

  final override type MonthName = GregorianMonthName

  // TODO stick it into the Month companion???
  val MonthName: GregorianMonthName.type = GregorianMonthName

  object Month extends MonthCompanion with GregorianCalendarMember {
    override def yearNumber(monthNumber: Int): Int =
      (monthNumber - 1) / Gregorian.Year.monthsInYear + 1

    override def numberInYear(monthNumber: Int): Int =
      monthNumber - Gregorian.Year.firstMonth(yearNumber(monthNumber)) + 1
  }


  final override type Day = GregorianDay

  final override def createDay(number: Int): Day =
    new GregorianDay(number) with GregorianCalendarMember

  final override type DayName = GregorianDayName

  // TODO stick it into the Day companion???
  val DayName: GregorianDayName.type = GregorianDayName

  final override val Day: GregorianDayCompanion =
    new GregorianDayCompanion with GregorianCalendarMember


  final class Moment(negative: Boolean, digits: List[Int])
    extends MomentBase(negative, digits) with GregorianCalendarMember
  {
    def morningHours(value: Int): Moment = firstHalfHours(value)

    def afternoonHours(value: Int): Moment = secondHalfHours(value)
  }

  final override def createMoment(negative: Boolean, digits: List[Int]): Moment =
    new Moment(negative, digits)

  object Moment extends MomentCompanion with GregorianCalendarMember
}


object Gregorian extends Gregorian
