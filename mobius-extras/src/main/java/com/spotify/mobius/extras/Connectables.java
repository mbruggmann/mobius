/*
 * -\-\-
 * Mobius
 * --
 * Copyright (c) 2017-2018 Spotify AB
 * --
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
 * -/-/-
 */
package com.spotify.mobius.extras;

import com.spotify.mobius.Connectable;
import com.spotify.mobius.Connection;
import com.spotify.mobius.ConnectionLimitExceededException;
import com.spotify.mobius.functions.Consumer;
import com.spotify.mobius.functions.Function;
import javax.annotation.Nonnull;

/** Contains utility functions for working with {@link Connectables}. */
public final class Connectables {

  private Connectables() {
    // prevent instantiation
  }

  /**
   * Apply a function to a {@link Connectable} in order to convert from one data type to another.
   * This is useful for instance if you want your UI to use a subset or a transformed version of the
   * full model used in the loop. The returned {@link Connectable} doesn't enforce a connection
   * limit, but of course the connection limit of the wrapped {@link Connectable} applies.
   *
   * @param mapper the mapping function to apply
   * @param connectable the underlying connectable
   * @param <M> the underlying type; usually the model
   * @param <E> the output type; usually the event type
   * @param <V> the type to convert to; called V as a mnemonic for ViewData.
   */
  @Nonnull
  public static <M, E, V> Connectable<M, E> map(
      final Function<M, V> mapper, final Connectable<V, E> connectable) {
    return new Connectable<M, E>() {
      @Nonnull
      @Override
      public Connection<M> connect(Consumer<E> output) throws ConnectionLimitExceededException {
        final Connection<V> delegateConnection = connectable.connect(output);

        return new Connection<M>() {
          @Override
          public void accept(M value) {
            delegateConnection.accept(mapper.apply(value));
          }

          @Override
          public void dispose() {
            delegateConnection.dispose();
          }
        };
      }
    };
  }
}
