package org.digitaljudaica.xml

import scala.xml.Elem

object Ops {

  implicit class Ops(elem: Elem) {

    def elemsFilter(name: String): Seq[Elem] = elem.elems.filter(_.label == name)

    // TODO dup!
    def elems: Seq[Elem] = elem.child.filter(_.isInstanceOf[Elem]).map(_.asInstanceOf[Elem])

    def elems(name: String): Seq[Elem] = {
      val result = elem.elems
      result.foreach(_.check(name))
      result
    }

    def descendants(name: String): Seq[Elem] = elem.flatMap(_ \\ name).filter(_.isInstanceOf[Elem]).map(_.asInstanceOf[Elem])

    def getAttribute(name: String): String = attributeOption(name).getOrElse(throw new NoSuchElementException(s"No attribute $name"))

    // TODO difference?
    def attributeOption(name: String): Option[String] = elem.attributes.asAttrMap.get(name)
    //    def attributeOption(name: String): Option[String] = {
    //      val result: Seq[Node] = elem \ ("@" + name)
    //      if (result.isEmpty) None else Some(result.text)
    //    }

    def idOption: Option[String] = attributeOption("xml:id")

    def id: String = getAttribute("xml:id")

    def oneChild(name: String): Elem = oneOptionalChild(name, required = true).get
    def optionalChild(name: String): Option[Elem] = oneOptionalChild(name, required = false)

    private[this] def oneOptionalChild(name: String, required: Boolean = true): Option[Elem] = {
      val children = elem \ name

      if (children.size > 1) throw new NoSuchElementException(s"To many children with name '$name'")
      if (required && children.isEmpty) throw new NoSuchElementException(s"No child with name '$name'")

      if (children.isEmpty) None else Some(children.head.asInstanceOf[Elem])
    }

    def check(name: String): Elem = {
      if (elem.label != name) throw new NoSuchElementException(s"Expected name $name but got $elem.label")
      elem
    }

    def withoutNamespace: Elem = Util.removeNamespace(elem)

    def format: String = Print.format(elem)
  }
}