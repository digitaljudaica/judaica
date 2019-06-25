package org.podval.docbook.gradle.xml

import org.podval.docbook.gradle.util.Logger
import org.xml.sax.helpers.XMLFilterImpl

final class ProcessingInstructionsFilter(
  substitutions: Map[String, String],
  logger: Logger
) extends XMLFilterImpl {
  override def processingInstruction(target: String, data: String): Unit = {
    logger.info(s"ProcessingInstructionsFilter.processingInstruction(target = $target, data = [$data])")
    if (target == "eval") {
      val expression: String = data.trim
      val result: String = substitutions.getOrElse(expression, s"Evaluation failed for [$expression]")

      val characters: Array[Char] = result.toCharArray
      this.characters(characters, 0, result.length)
    } else {
      super.processingInstruction(target, data)
    }
  }
}
