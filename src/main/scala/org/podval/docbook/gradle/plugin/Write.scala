package org.podval.docbook.gradle.plugin

import java.io.{BufferedWriter, File, FileWriter}

import org.podval.docbook.gradle.fop.Fop
import org.podval.docbook.gradle.section.{DocBook2, HtmlCommon, Section}
import org.podval.docbook.gradle.util.Logger
import org.podval.docbook.gradle.xml.{Namespace, Xml}

final class Write(val layout: Layout, val logger: Logger) {

  def inputFile(documentName: String, content: String =
    s"""${Xml.header}
       |${DocBook.doctype}
       |
       |<article ${DocBook.Namespace.withVersion}
       |         ${Namespace.XInclude}>
       |</article>
       |"""
  ): Unit = writeInto(layout.inputFile(documentName), replace = false)(content)

  def css(cssFileName: String): Unit = writeInto(layout.cssFile(cssFileName), replace = false) {
    s"""@namespace xml "${Namespace.Xml.uri}";
       |"""
  }

  def writeFopConfiguration(): Unit = writeInto(layout.fopConfigurationFile, replace = false) {
    Fop.defaultConfigurationFile
  }

  def substitutionsDtd(substitutions: Map[String, String]): Unit =
    writeInto(layout.xmlFile(layout.substitutionsDtdFileName)) {
      substitutions.toSeq.map {
        case (name: String, value: String) => s"""<!ENTITY $name "$value">\n"""
      }.mkString
    }

  def xmlCatalog(): Unit = writeInto(layout.catalogFile) {
    val data: String = layout.dataDirectoryRelative

    s"""${Xml.header}
       |<!DOCTYPE catalog
       |  PUBLIC "-//OASIS//DTD XML Catalogs V1.1//EN"
       |  "http://www.oasis-open.org/committees/entity/release/1.1/catalog.dtd">
       |
       |<!-- DO NOT EDIT! Generated by the DocBook plugin.
       |     Customizations go into ${layout.catalogCustomFileName}. -->
       |<catalog xmlns="urn:oasis:names:tc:entity:xmlns:xml:catalog" prefer="public">
       |  <group xml:base="${layout.catalogGroupBase}">
       |    <!--
       |      There seems to be some confusion with the rewriteURI form:
       |      Catalog DTD requires 'uriIdStartString' attribute (and that is what IntelliJ wants),
       |      but XMLResolver looks for the 'uriStartString' attribute (and this seems to work in Oxygen).
       |    -->
       |
       |    <!-- DocBook XSLT 1.0 stylesheets  -->
       |    <rewriteURI uriStartString="http://docbook.sourceforge.net/release/xsl-ns/current/"
       |                rewritePrefix="${layout.docBookXslDirectoryRelative(Stylesheets.xslt1.directoryName)}"/>
       |
       |    <!-- DocBook XSLT 2.0 stylesheets  -->
       |    <rewriteURI uriStartString="https://cdn.docbook.org/release/latest/xslt/"
       |                rewritePrefix="${layout.docBookXslDirectoryRelative(Stylesheets.xslt2.directoryName)}"/>
       |
       |    <!-- generated data -->
       |    <rewriteSystem systemIdStartString="data:/"
       |                   rewritePrefix="$data"/>
       |    <rewriteSystem systemIdStartString="data:"
       |                   rewritePrefix="$data"/>
       |    <rewriteSystem systemIdStartString="urn:docbook:data:/"
       |                   rewritePrefix="$data"/>
       |    <rewriteSystem systemIdStartString="urn:docbook:data:"
       |                   rewritePrefix="$data"/>
       |    <rewriteSystem systemIdStartString="urn:docbook:data/"
       |                   rewritePrefix="$data"/>
       |    <rewriteSystem systemIdStartString="http://podval.org/docbook/data/"
       |                   rewritePrefix="$data"/>
       |  </group>
       |
       |  <!-- substitutions DTD -->
       |  <public publicId="${DocBook.dtdId}"
       |          uri="${layout.substitutionsDtdFileName}"/>
       |
       |  <nextCatalog catalog="${layout.catalogCustomFileName}"/>
       |</catalog>
       |"""
  }

  def xmlCatalogCustomization(): Unit =
    writeInto(layout.xmlFile(layout.catalogCustomFileName), replace = false) {
      s"""${Xml.header}
         |<!DOCTYPE catalog
         |  PUBLIC "-//OASIS//DTD XML Catalogs V1.1//EN"
         |  "http://www.oasis-open.org/committees/entity/release/1.1/catalog.dtd">
         |
         |<!-- Customizations go here. -->
         |<catalog xmlns="urn:oasis:names:tc:entity:xmlns:xml:catalog" prefer="public">
         |  <nextCatalog catalog="/etc/xml/catalog"/>
         |</catalog>
         |"""
    }

  def mainStylesheet(
    docBook2: DocBook2,
    prefixed: Boolean,
    documentName: String,
    cssFileName: String,
    epubEmbeddedFonts: String
  ): Unit = {
    val forDocument = layout.forDocument(prefixed, documentName)

    val mainStylesheetName: String = forDocument.mainStylesheet(docBook2)
    val paramsStylesheetName: String = layout.paramsStylesheet(docBook2)
    val stylesheetUri: String = s"${Stylesheets(docBook2.usesDocBookXslt2).uri}/${docBook2.stylesheetUriName}.xsl"

    val nonOverridableParameters: Map[String, String] = Seq[Option[(String, String)]](
      Some("img.src.path", layout.imagesDirectoryName + "/"),
      docBook2.parameter(_.baseDirParameter, forDocument.baseDir(docBook2)),
      docBook2.parameter(_.rootFilenameParameter, docBook2.rootFilename(documentName)),
      docBook2.parameter(_.epubEmbeddedFontsParameter, epubEmbeddedFonts),
      docBook2.parameter(_.htmlStylesheetsParameter, layout.cssFileRelativeToOutputDirectory(cssFileName))
    ).flatten.toMap

    // xsl:param has the last value assigned to it, so customization must come last;
    // since it is imported (so as not to be overwritten), and import elements must come first,
    // a separate "-param" file is written with the "default" values for the parameters :)

    val imports: String = docBook2.parameterSections.map(section =>
      s"""  <xsl:import href="${layout.customStylesheet(section)}"/>"""
    ).mkString("\n")

    writeInto(layout.stylesheetFile(mainStylesheetName)) {
      s"""${Xml.header}
         |<!-- DO NOT EDIT! Generated by the DocBook plugin. -->
         |<xsl:stylesheet ${Namespace.Xsl.withVersion(docBook2.xsltVersion)}>
         |  <xsl:import href="$stylesheetUri"/>
         |  <xsl:import href="$paramsStylesheetName"/>
         |$imports
         |
         |${toString(nonOverridableParameters)}
         |</xsl:stylesheet>
         |"""
    }
  }

  def paramsStylesheet(
    docBook2: DocBook2,
    sections: Map[Section, Map[String, String]]
  ): Unit = {
    val paramsStylesheetName: String = layout.paramsStylesheet(docBook2)

    val dynamicParameters: Map[Section, Map[String, String]] = Map.empty
      .updated(HtmlCommon, Seq[Option[(String, String)]](
        if (logger.isInfoEnabled) None else
          docBook2.parameter(_.chunkQuietlyParameter, "1")
      ).flatten.toMap)

    val parameters: Map[Section, Map[String, String]] = docBook2.parameterSections.map { section: Section => section -> (
      section.defaultParameters ++
        dynamicParameters.getOrElse(section, Map.empty) ++
        sections.getOrElse(section, Map.empty)
      )}.toMap
    //      .reduceLeft[Map[String, String]] { case (result, current) => result ++ current }

    val parametersStr: String = docBook2.parameterSections.map { section: Section =>
      s"  <!-- ${section.name} -->\n" + toString(parameters.getOrElse(section, Map.empty))
    }.mkString("\n")

    writeInto(layout.stylesheetFile(paramsStylesheetName)) {
      s"""${Xml.header}
         |<!-- DO NOT EDIT! Generated by the DocBook plugin. -->
         |<xsl:stylesheet ${Namespace.Xsl.withVersion(docBook2.xsltVersion)}>
         |$parametersStr
         |</xsl:stylesheet>
         |"""
    }
  }

  def customStylesheet(
    section: Section
  ): Unit = {
    val customStylesheetName: String = layout.customStylesheet(section)

    writeInto(layout.stylesheetFile(customStylesheetName), replace = false) {
      s"""${Xml.header}
         |<!-- Customizations go here. -->
         |<xsl:stylesheet
         |  ${Namespace.Xsl.withVersion(section.xsltVersion)}
         |  xmlns:db="${DocBook.Namespace.uri}"
         |  exclude-result-prefixes="db">
         |
         |${section.customStylesheet}
         |</xsl:stylesheet>
         |"""
    }
  }

  def settingsGradle(pluginDir: File): Unit =
    writeInto(layout.settingsGradle)(s"includeBuild '$pluginDir'")

  def buildGradle(content: String): Unit =
    writeInto(layout.buildGradle)(content)

  private def writeInto(file: File, replace: Boolean = true)(content: String): Unit = {
    if (!replace && file.exists) {
//      logger.info(s"Already exists: $file")
    } else {
//      logger.info(s"Writing $file")
      file.getParentFile.mkdirs()
      val writer: BufferedWriter = new BufferedWriter(new FileWriter(file))

      try {
        writer.write(content.stripMargin)
      } finally {
        writer.close()
      }
    }
  }

  private def toString(parameters: Map[String, String]): String = parameters.map { case (name: String, value: String) =>
    if (value.nonEmpty) s"""  <xsl:param name="$name">$value</xsl:param>"""
    else s"""  <xsl:param name="$name"/>"""
  }.mkString("\n")
}
