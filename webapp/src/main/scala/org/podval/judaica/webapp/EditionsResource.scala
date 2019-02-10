/*
 *  Copyright 2014 Leonid Dubinsky <dub@podval.org>.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.podval.judaica.webapp

import org.podval.judaica.viewer.{Work, Edition, Editions, Selection}
import javax.ws.rs.{Produces, PathParam, Path, GET}
import javax.ws.rs.core.{MediaType, UriInfo, Context}


final class EditionsResource(work: Work) {

  import EditionsResource._


  @GET
  @Produces(Array(MediaType.TEXT_HTML))
  def editions(@Context uriInfo: UriInfo) = Html(uriInfo, Table(work.editions, uriInfo, editionsColumn))


  @Path("{editions}")
  def selection(@PathParam("editions") editionNames: String) = new StructureSelectionResource(Selection(work, editionNames))


  @GET
  @Path("/{edition}/stylesheet.css")
  @Produces(Array("text/css"))
  def stylesheet(@PathParam("edition") editionName: String) = work.getEditionByName(editionName).stylesheet
}



object EditionsResource {

  val editionsColumn: LinkColumn[Edition] = new SimpleLinkColumn[Edition]("Editions") {
    override def text(edition: Edition): String = edition.defaultName
  }
}