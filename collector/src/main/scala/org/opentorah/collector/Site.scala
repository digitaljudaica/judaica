package org.opentorah.collector

import org.opentorah.html
import org.opentorah.html.Html
import org.opentorah.site.{HtmlContent, SiteCommon, Viewer}
import org.opentorah.store.{Caching, Directory, ListFile, Store, WithSource}
import org.opentorah.tei.{EntityReference, EntityType, LinksResolver, Tei, Unclear}
import org.opentorah.util.{Effects, Files}
import org.opentorah.xml.{FromUrl, Parser, Xml}
import zio.{UIO, ZIO}
import java.net.URL

// TODO retrieve TEI(?) references from notes.
final class Site(
  fromUrl: FromUrl,
  common: SiteCommon,
  val entities: Entities,
  val entityLists: EntityLists,
  val notes: Notes,
  val by: ByHierarchy
) extends org.opentorah.site.Site[Site](
  fromUrl,
  common
) {
  private val paths: Seq[Store.Path] = getPaths(Seq.empty, by)

  private def getPaths(path: Store.Path, by: ByHierarchy): Seq[Store.Path] = by.stores.flatMap { store =>
    val storePath: Store.Path = path ++ Seq(by, store)
    Seq(storePath) ++ (store match {
      case hierarchy : Hierarchy  => getPaths(storePath, hierarchy.by)
      case _ => Seq.empty
    })
  }

  val store2path: Map[Store, Store.Path] = paths.map(path => path.last -> path).toMap

  val collections: Seq[Collection] = for {
    path <- paths
    last = path.last
    if last.isInstanceOf[Collection]
  } yield last.asInstanceOf[Collection]

  private val hierarchies: Seq[Hierarchy] = for {
    path <- paths
    last = path.last
    if last.isInstanceOf[Hierarchy]
  } yield last.asInstanceOf[Hierarchy]

  private val alias2collectionAlias: Map[String, Collection.Alias] = collections
    .filter(_.alias.isDefined)
    .map(collection => collection.alias.get -> new Collection.Alias(collection))
    .toMap

  private val references: ListFile[WithSource[EntityReference], Seq[WithSource[EntityReference]]] = WithSource(
    url = Files.fileInDirectory(fromUrl.url, "references-generated.xml"),
    name = "references",
    value = EntityReference
  )
  def getReferences: Caching.Parser[Seq[WithSource[EntityReference]]] = references.get

  private val unclears: ListFile[WithSource[Unclear.Value], Seq[WithSource[Unclear.Value]]] = WithSource(
    url = Files.fileInDirectory(fromUrl.url, "unclears-generated.xml"),
    name = "unclears",
    value = Unclear.element
  )
  def getUnclears: Caching.Parser[Seq[WithSource[Unclear.Value]]] = unclears.get

  override def defaultViewer: Option[Viewer] = Some(Viewer.default)

  override protected def index: Caching.Parser[Option[Store.Path]] = ZIO.some(Seq(Index.Flat))

  // TODO ZIOify logging!
  //info(request, storePath.fold(s"--- ${Files.mkUrl(path)}")(storePath => s"YES ${storePath.mkString("")}"))

  override protected def content(path: Store.Path): Caching.Parser[org.opentorah.site.Site.Response] = path.lastOption.getOrElse(this) match {
    case teiFacet   : Document.TeiFacet =>
      teiFacet.getTei.map(renderTeiContent).map(content => new org.opentorah.site.Site.Response(content, Tei.mimeType))
    case htmlContent: HtmlContent[Site] =>
      renderHtmlContent(htmlContent).map(content => new org.opentorah.site.Site.Response(content, Html.mimeType))
  }

  override def style(htmlContent: HtmlContent[Site]): String = htmlContent match {
    case _: Collection.Alias => "wide"
    case _: Collection       => "wide"
    case _                   => "main"
  }

  override def viewer(htmlContent: HtmlContent[Site]): Option[org.opentorah.site.Viewer] = htmlContent match {
    case _: Collection.Alias        => Some(Viewer.Collection)
    case _: Collection              => Some(Viewer.Collection)
    case _: Document.TextFacet      => Some(Viewer.Document  )
    case _: Document.FacsimileFacet => Some(Viewer.Facsimile )
    case _: EntityLists             => Some(Viewer.Names     )
    case _: Entity                  => Some(Viewer.Names     )
    case Reports                    => Some(Viewer.Names     )
    case _                          => defaultViewer
  }

  override protected def navigationLinks(htmlContent: HtmlContent[Site]): Caching.Parser[Seq[Xml.Element]] = htmlContent match {
    case collectionAlias: Collection.Alias => collectionNavigationLinks(collectionAlias.collection)
    case collection: Collection => collectionNavigationLinks(collection)

    case htmlFacet: Document.HtmlFacet[_, _] => for {
      siblings <- htmlFacet.collection.siblings(htmlFacet.document)
      collection = htmlFacet.collection
      document = htmlFacet.document
      collectionFacet = htmlFacet.collectionFacet
      collectionNavigationLinks <- collectionNavigationLinks(collection)
      moreLinks <- htmlFacet match {
        case _: Document.TextFacet =>
          collection.translations(htmlFacet.document).map { translations =>
            Seq(a(collection.facsimileFacet.of(htmlFacet.document))(text = Tei.facsimileSymbol)) ++ {
              for (translation <- if (document.isTranslation) Seq.empty else translations)
                yield a(collectionFacet.of(translation))(s"[${translation.lang}]")
            }
          }
        case _: Document.FacsimileFacet =>
          ZIO.succeed(Seq(a(collection.textFacet.of(document))(text = "A")))
      }
    } yield {
      val (prev: Option[Document], next: Option[Document]) = siblings

      collectionNavigationLinks ++
        prev.toSeq.map(prev => a(collectionFacet.of(prev    ))("⇦"          )) ++
        Seq(                   a(collectionFacet.of(document))(document.name)) ++
        next.toSeq.map(next => a(collectionFacet.of(next    ))("⇨"          )) ++
        moreLinks
    }

    case _ => ZIO.succeed (Seq.empty)
  }

  private def collectionNavigationLinks(collection: Collection): Caching.Parser[Seq[Xml.Element]] =
    ZIO.succeed(Seq(a(collection)(s"[${collection.names.name}]")))

  override protected def path(htmlContent: HtmlContent[Site]): Store.Path = htmlContent match {
    case textFacet      : Document.TextFacet      =>
      path(textFacet.collection     ) ++ Seq(                                          textFacet     )
    case facsimileFacet : Document.FacsimileFacet =>
      path(facsimileFacet.collection) ++ Seq(facsimileFacet.collection.facsimileFacet, facsimileFacet)

    case collection     : Collection  =>
      collection.alias.fold(store2path(collection))(alias => Seq(alias2collectionAlias(alias)))

    case collectionAlias: Collection.Alias => Seq(collectionAlias)
    case index          : Index            => Seq(index)
    case hierarchy      : Hierarchy        => store2path(hierarchy)
    case entityLists    : EntityLists      => Seq(entityLists)
    case entity         : Entity           => Seq(entities, entity)
    case notes          : Notes            => Seq(notes)
    case note           : Note             => Seq(notes, note)
    case Reports                           => Seq(Reports)
    case report         : Report[_]        => Seq(Reports, report)
  }

  override def findByName(name: String): Caching.Parser[Option[Store]] =
    ZIO.succeed(alias2collectionAlias.get(name)) >>= {
      case Some(result) => ZIO.some(result)
      case None =>
        Store.findByName(name, Seq(entities, notes, Reports, by)) >>= {
          case Some(result) => ZIO.some(result)
          case None => Store.findByName(
            name,
            "html",
            name => Store.findByName(name, Seq(Index.Flat, Index.Tree, entityLists))
          )
        }
    }

  override protected def linkResolver(htmlContent: org.opentorah.site.HtmlContent[org.opentorah.collector.Site]): LinksResolver = {
    val textFacet: Option[Document.TextFacet] = htmlContent match {
      case htmlFacet: Document.TextFacet => Some(htmlFacet)
      case _ => None
    }

    new LinksResolver {
      private val facsUrl: Option[Store.Path] = textFacet.map(textFacet =>
        path(textFacet.collection.facsimileFacet.of(textFacet.document)))

      def toUIO(parser: Caching.Parser[Option[html.a]], error: => String): UIO[Option[html.a]] =
        toTask(parser.map { a =>
          if (a.isEmpty) logger.warn(error)
          a
        }).orDie

      override def resolve(url: Seq[String]): UIO[Option[html.a]] = toUIO(
        Site.this.resolve(url).map(_.map(path => a(path))),
        s"did not resolve: $url"
      )

      override def findByRef(ref: String): UIO[Option[html.a]] = toUIO(
        entities.findByName(ref).map(_.map(entity => entity.a(Site.this))),
        s"did not find reference: $ref"
      )

      override def facs(pageId: String): UIO[Option[html.a]] = toUIO(
        ZIO.succeed(facsUrl.map(facsUrl => a(facsUrl).setFragment(pageId))),
        "did not get facsimile: $pageId"
      )
    }
  }

  override protected def directoriesToWrite: Seq[Directory[_, _, _]] = Seq(entities, notes) ++ collections

  override protected def buildMore: Caching.Parser[Unit] = for {
      _ <- Effects.effect(logger.info("Writing references."))
      allReferences <- allWithSource[EntityReference](
        nodes => ZIO.foreach(EntityType.values)(entityType =>
          Xml.descendants(nodes, entityType.nameElement, EntityReference)).map(_.flatten)
      )
      _ <- Effects.effect(references.write(allReferences))

      _ <- Effects.effect(logger.info("Writing unclears."))
      allUnclears <- allWithSource[Unclear.Value](
        nodes => Xml.descendants(nodes, Unclear.element.elementName, Unclear.element)
      )
      _ <- Effects.effect(unclears.write(allUnclears))

      _ <- Effects.effect(logger.info("Verifying site."))

      errorOpts <- getReferences >>= (ZIO.foreach(_) { value =>
        val reference: EntityReference = value.value
        val name: Xml.Nodes = reference.name
        reference.ref.fold[Caching.Parser[Option[String]]](ZIO.none)(ref =>
          if (ref.contains(" ")) ZIO.some(s"""Value of the ref attribute contains spaces: ref="$ref" """)
          else entities.findByName(ref).map(_
            .fold[Option[String]](Some(s"""Unresolvable reference: Name ref="$ref">${name.text}< """))(named =>
              if (named.entityType == reference.entityType) None
              else Some(s"${reference.entityType} reference to ${named.entityType} ${named.name}: $name [$ref]")
            )
          )
        )
      })
      errors = errorOpts.flatten
      _ <- Effects.check(errors.isEmpty, errors.mkString("\n"))

      // detect and log unresolved references
      allNotes <- notes.directoryEntries
      allEntities <- entities.directoryEntries
      _ <- ZIO.foreach_(hierarchies ++ collections ++ allNotes ++ allEntities)(resolveHtmlContent)

      _ <- ZIO.foreach_(collections)(collection => collection.directoryEntries >>= (ZIO.foreach_(_)(document =>
          resolveHtmlContent(collection.textFacet.of(document)))))
    } yield ()

  def allWithSource[T](finder: Xml.Nodes => Parser[Seq[T]]): Caching.Parser[Seq[WithSource[T]]] = {

    def withSource(htmlContent: HtmlContent[Site], nodes: Xml.Nodes): Parser[Seq[WithSource[T]]] = {
      val source: String = Files.mkUrl(path(htmlContent).map(_.structureName))
      finder(nodes).map(_.map(new WithSource[T](source, _)))
    }

    for {
      entities <- entities.directoryEntries
      fromEntities <- ZIO.foreach(entities)(entity =>
        entity.teiEntity(this) >>= (teiEntity => withSource(entity, teiEntity.content)))
      fromHierarchicals <- ZIO.foreach(hierarchies ++ collections) { hierarchical => withSource(
        hierarchical,
        Seq(Some(hierarchical.title), hierarchical.storeAbstract, hierarchical.body).flatten.flatMap(_.xml)
      )}
      fromDocuments <- ZIO.foreach(collections) { collection =>
        collection.directoryEntries >>= (documents => ZIO.foreach(documents) { document =>
          val text: Document.TextFacet = collection.textFacet.of(document)
          text.getTei >>= (tei => withSource(text, Seq(Tei.xmlElement(tei))))
        })
      }
    } yield (fromEntities ++ fromHierarchicals ++ fromDocuments.flatten).flatten
  }

  override protected def prettyPrintRoots: Seq[URL] = Seq(
//    fromUrl.url,  // leave the site.xml file alone :)
    entities.directoryUrl,
    Files.subdirectory(fromUrl.url, "archive") // TODO do not assume the directory name!
  )
}

