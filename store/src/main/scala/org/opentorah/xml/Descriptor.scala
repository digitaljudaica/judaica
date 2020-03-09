package org.opentorah.xml

import scala.xml.Node

abstract class Descriptor[A](
  val elementName: String,
  val contentType: ContentType = ContentType.Elements,
  val parser: Parser[A]
) extends Element[A](
  elementName,
  contentType,
  parser
) with ToXml[A] {

  // TODO move elsewhere?
  final def parse(from: From): Parser[A] =
    from.parse(contentType, Element.withName(elementName, parser))

  // TODO move elsewhere
  final def descendants(xml: Node): Seq[A] =
    XmlUtil.descendants(xml, elementName).map(xml =>
      Parser.parseDo(parse(From.xml(xml)))
    )
}