/*
 * Copyright (c) 2005, 2013, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package org.sample;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.RecursiveTask;
import java.util.concurrent.TimeUnit;

import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.GenerateMicroBenchmark;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;

public class MyBenchmark {

  @State(Scope.Thread)
  public static class ForkJoinBenchmarkTask {
    long N = Long.getLong("tasks");
    ForkJoinTask<Long> recursiveTask = new RecursiveForkJoinTask(N);
    ForkJoinTask<Long> recursiveBlockingTask = new RecursiveBlockingForkJoinTask(N);
  }

  @State(Scope.Benchmark)
  public static class ForkJoinBenchmarkState {
    ForkJoinPool pool = new ForkJoinPool(Runtime.getRuntime().availableProcessors());
  }

  @GenerateMicroBenchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  public Long testRecurse(ForkJoinBenchmarkState state, ForkJoinBenchmarkTask task) throws ExecutionException, InterruptedException {
    return state.pool.invoke(task.recursiveTask); // baseline
  }

  @GenerateMicroBenchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  public Long testBlockRecurse(ForkJoinBenchmarkState state, ForkJoinBenchmarkTask task) throws ExecutionException, InterruptedException {
    state.pool.submit(task.recursiveBlockingTask); //generate load, with blocking tasks.
    return state.pool.invoke(task.recursiveTask);
  }

  @GenerateMicroBenchmark
  @BenchmarkMode(Mode.SingleShotTime)
  @OutputTimeUnit(TimeUnit.MICROSECONDS)
  public Long testBlockPauseRecurse(ForkJoinBenchmarkState state, ForkJoinBenchmarkTask task) throws ExecutionException, InterruptedException {
    state.pool.submit(task.recursiveBlockingTask); //generate load, with blocking tasks.
    Thread.sleep(100); // allow recursiveBlockingTask to consume more parallelism
    return state.pool.invoke(task.recursiveTask);
  }

  static class RecursiveForkJoinTask extends RecursiveTask<Long> {
    private long n;

    public RecursiveForkJoinTask(long n) {
      this.n = n;
    }

    @Override
    protected Long compute() {
      if (n <= 1) {
        return n;
      }
      RecursiveForkJoinTask f1 = new RecursiveForkJoinTask(n / 2);
      f1.fork();
      RecursiveForkJoinTask f2 = new RecursiveForkJoinTask(n / 2);
      return f2.compute() + f1.join();
    }
  }


  static class RecursiveBlockingForkJoinTask extends RecursiveTask<Long> {
    private static long threshold = 128;
    private final long n;

    public RecursiveBlockingForkJoinTask(long n) {
      this.n = n;
    }

    @Override
    protected Long compute() {
      if (n <= threshold) {
        try {
          Thread.sleep(threshold); // this
        }
        catch (InterruptedException e) {
        }
        return n;
      }

      RecursiveBlockingForkJoinTask f1 = new RecursiveBlockingForkJoinTask(n / 2);
      f1.fork();
      RecursiveBlockingForkJoinTask f2 = new RecursiveBlockingForkJoinTask(n / 2);
      return f2.compute() + f1.join();
    }

  }
}
