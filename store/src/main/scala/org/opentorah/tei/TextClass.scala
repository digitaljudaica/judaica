package org.opentorah.tei

import org.opentorah.xml.RawXml
import scala.xml.Node

final case class TextClass(xml: Seq[Node]) extends RawXml(xml)

object TextClass extends RawXml.Descriptor[TextClass]("textClass", new TextClass(_))