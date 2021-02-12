package org.opentorah.tei

import org.opentorah.xml.{Unparser, Attribute, ContentType, Element, From, Parsable, Parser, Xml}

final case class EntityReference(
  entityType: EntityType,
  name: Xml.Nodes,
  id: Option[String],
  role: Option[String],
  ref: Option[String]
)

object EntityReference extends EntityRelated[EntityReference](
  elementName = _.nameElement,
  entityType = _.entityType
) {
  override protected def contentType: ContentType = ContentType.Mixed

  private val idAttribute: Attribute.Optional[String] = Xml.idAttribute.optional
  private val roleAttribute: Attribute.Optional[String] = Attribute("role").optional
  private val refAttribute: Attribute.Optional[String] = Attribute("ref").optional
  private val typeAttribute: Attribute.Optional[String] = Attribute("type").optional

  override protected def parsable(entityType: EntityType): Parsable[EntityReference] = new Parsable[EntityReference] {
    override protected def parser: Parser[EntityReference] = for {
      id <- idAttribute()
      role <- roleAttribute()
      ref <- refAttribute()
      _ <- typeAttribute() // We don't do anything with the type yet...
      name <- Element.nodes()
    } yield new EntityReference(
      entityType,
      name,
      id,
      role,
      ref
    )

    override def unparser: Unparser[EntityReference] = Tei.concat(
      refAttribute(_.ref),
      idAttribute(_.id),
      roleAttribute(_.role),
      Element.nodes(_.name)
    )
  }

  // TODO eliminate?
  def fromXml(xml: Xml.Nodes): Seq[EntityReference] = for {
    entityType <- EntityType.values
    node <- xml
    descendant <- Xml.descendants(node, entityType.nameElement)
  } yield
    Parser.parseDo(EntityReference.parse(From.xml("descendants", descendant)))
}
