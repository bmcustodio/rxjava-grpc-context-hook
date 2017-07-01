/*
 * Copyright 2016-2017 brunomcustodio
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.bmcstdio.rxjava.hooks;

import io.grpc.Context;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import rx.Observable;
import rx.observers.TestSubscriber;
import rx.plugins.RxJavaHooks;
import rx.schedulers.Schedulers;

@RunWith(JUnit4.class)
public final class GrpcContextPropagatingOnScheduleActionTests {
  private static final Context.Key<String> KEY_1 = Context.key("KEY_1");
  private static final String VAL_1 = "VAL_1";
  private static final Context.Key<Double> KEY_2 = Context.key("KEY_2");
  private static final Double VAL_2 = Math.PI;

  @AfterClass
  public static void afterClass() {
    RxJavaHooks.setOnScheduleAction(null);
  }

  @BeforeClass
  public static void beforeClass() {
    RxJavaHooks.setOnScheduleAction(new GrpcContextPropagatingOnScheduleAction());
  }

  @Test
  public void doesPropagateContext() throws Exception {
    final Context oldContext = Context.current();
    final Context newContext = oldContext.withValues(KEY_1, VAL_1, KEY_2, VAL_2);

    newContext.attach();

    final TestSubscriber<Object> subscriber = new TestSubscriber<Object>();
    Observable.create(subscriber1 -> {
      subscriber1.onNext(KEY_1.get());
      subscriber1.onNext(KEY_2.get());
      subscriber1.onCompleted();
    }).subscribeOn(Schedulers.computation()).subscribe(subscriber);

    newContext.detach(oldContext);

    subscriber.awaitTerminalEvent();
    subscriber.assertValues(VAL_1, VAL_2);
  }
}
