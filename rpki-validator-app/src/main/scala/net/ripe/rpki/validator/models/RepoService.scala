/**
 * The BSD License
 *
 * Copyright (c) 2010-2012 RIPE NCC
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *   - Redistributions of source code must retain the above copyright notice,
 *     this list of conditions and the following disclaimer.
 *   - Redistributions in binary form must reproduce the above copyright notice,
 *     this list of conditions and the following disclaimer in the documentation
 *     and/or other materials provided with the distribution.
 *   - Neither the name of the RIPE NCC nor the names of its contributors may be
 *     used to endorse or promote products derived from this software without
 *     specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package net.ripe.rpki.validator.models

import java.net.URI

import net.ripe.rpki.validator.fetchers.Fetcher
import net.ripe.rpki.validator.models.validation.RepoFetcher
import net.ripe.rpki.validator.store.{DataSources, RepoServiceStore}
import org.joda.time.{Duration, Instant}


class RepoService(fetcher: RepoFetcher) {
  val UPDATE_INTERVAL = Duration.standardMinutes(5) //TODO

  val store = new RepoServiceStore(DataSources.InMemoryDataSource) // TODO

  def visitRepo(uri: URI): Seq[Fetcher.Error] = {
    if (haveRecentDataInStore(uri)) Seq()
    else fetchAndUpdateTime(uri)
  }

  protected[models] def fetchAndUpdateTime(uri: URI): Seq[Fetcher.Error] = {
    val fetchTime = Instant.now()
    val result = fetcher.fetch(uri)
    store.updateLastFetchTime(uri, fetchTime)
    result
  }

  def visitObject(uri: URI) = fetcher.fetchObject(uri)

  private def haveRecentDataInStore(uri: URI): Boolean = {
    timeIsRecent(store.getLastFetchTime(uri), UPDATE_INTERVAL)
  }

  private[models] def timeIsRecent(dateTime: Instant, duration: Duration): Boolean = dateTime.plus(duration).isAfterNow
}