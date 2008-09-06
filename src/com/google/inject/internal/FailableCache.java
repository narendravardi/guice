/**
 * Copyright (C) 2008 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.inject.internal;

/**
 * Lazily creates (and caches) values for keys. If creating the value fails (with errors), an
 * exception is thrown on retrieval.
 *
 * @author jessewilson@google.com (Jesse Wilson)
 */
public abstract class FailableCache<K, V> {
  
  private final ReferenceCache<K, Object> delegate = new ReferenceCache<K, Object>() {
    protected final Object create(K key) {
      Errors errors = new Errors();
      V result = null;
      try {
        result = FailableCache.this.create(key, errors);
      } catch (ErrorsException e) {
        errors.merge(e.getErrors());
      }
      return errors.hasErrors() ? errors.makeImmutable() : result;
    }
  };

  protected abstract V create(K key, Errors errors) throws ErrorsException;
  
  public V get(K key, Errors errors) throws ErrorsException {
    Object resultOrError = delegate.get(key);
    if (resultOrError instanceof Errors) {
      errors.merge((Errors) resultOrError);
      throw errors.toException();
    } else {
      return (V) resultOrError;
    }
  }
}