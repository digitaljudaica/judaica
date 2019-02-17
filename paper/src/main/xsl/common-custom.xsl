<?xml version="1.0" encoding="UTF-8"?>
<!-- Customizations go here. -->
<xsl:stylesheet
  xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="1.0"
  xmlns:db="http://docbook.org/ns/docbook"
  exclude-result-prefixes="db">

  <!-- This is needed (?) for template-tweaking customizations, like removal of "Chapter" in chapter title -->
  <xsl:param name="local.l10n.xml" select="document('')"/>

  <!-- Remove "Chapter" in chapter title -->
  <l:i18n xmlns:l="http://docbook.sourceforge.net/xmlns/l10n/1.0">
    <l:l10n language="en">
      <l:context name="title-numbered">
        <l:template name="chapter" text="%n.&#160;%t"/>
      </l:context>
    </l:l10n>
  </l:i18n>

</xsl:stylesheet>
