package org.opentorah.tei

import org.opentorah.xml.{Antiparser, Attribute, ContentType, Element, Parser, Xml}

final case class Pb(
  n: String,
  id: Option[String],
  facs: Option[String],
  isMissing: Boolean = false,
  isEmpty: Boolean = false
) {
  def addAttributes(element: Xml.Element): Xml.Element = Xml.setAttributes(Xml.getAttributes(element) ++ Seq(
    Pb.missingAttribute.withOptionalValue(Some(isMissing)),
    Pb.emptyAttribute.withOptionalValue(Some(isEmpty))
  ), element)
}

object Pb extends Element[Pb]("pb") {

  val nAttribute: Attribute[String] = Attribute("n")
  private val missingAttribute: Attribute.BooleanAttribute = new Attribute.BooleanAttribute("missing")
  private val emptyAttribute: Attribute.BooleanAttribute = new Attribute.BooleanAttribute("empty")
  private val facsAttribute: Attribute[String] = Attribute("facs")

  override def contentType: ContentType = ContentType.Empty

  override val parser: Parser[Pb] = for {
    n <- nAttribute.required
    id <- Xml.idAttribute.optional
    facs <- facsAttribute.optional
    isMissing <- missingAttribute.optionalOrDefault
    isEmpty <- emptyAttribute.optionalOrDefault
  } yield new Pb(
    n,
    id,
    facs,
    isMissing,
    isEmpty
  )

  override val antiparser: Antiparser[Pb] = Tei.concat(
    nAttribute.toXml(_.n),
    Xml.idAttribute.toXmlOption(_.id),
    facsAttribute.toXmlOption(_.facs),
    missingAttribute.toXml(_.isMissing),
    emptyAttribute.toXml(_.isEmpty)
  )
}
