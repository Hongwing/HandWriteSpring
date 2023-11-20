/*
 * MIT License
 *
 * Copyright (c) 2023 Henry HE (henryhe.cn)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package cn.henryhe.io;

import java.util.concurrent.*;

/**
 * 测试CompletableFuture
 */
public class CompletableFutureTest {

    public static void testFuture() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        Future<String> result = executorService.submit(() -> {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "hello";
        });
        System.out.println(result.get());
        executorService.shutdown();
    }

    /**
     * Future 结合 CountDownLatch 实现异步任务的同步化
     * @throws ExecutionException
     * @throws InterruptedException
     */
    public static void testFutureASync() throws ExecutionException, InterruptedException {
        ExecutorService executorService = Executors.newFixedThreadPool(5);
        CountDownLatch countDownLatch = new CountDownLatch(2);
        long startTime = System.currentTimeMillis();

        Future<String> userFuture = executorService.submit(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
//            countDownLatch.countDown();
            return "userInfo";
        });
        Future<String> bookFuture = executorService.submit(() -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
//            countDownLatch.countDown();
            return "bookInfo";
        });
        // 使用countdownLatch：就是需要异步任务结果的地方可以拿到结果（计数器以确保异步执行完成）
//        countDownLatch.await();
        Thread.sleep(200);
        System.out.println("mainProcess");
        Thread.sleep(200);
        System.out.println(userFuture.get());
        System.out.println(bookFuture.get());
        System.out.println(System.currentTimeMillis()  - startTime);
        executorService.shutdown();
    }

    public static void testCompletableFuture() throws ExecutionException, InterruptedException {
        long startTime = System.currentTimeMillis();

        CompletableFuture<String> userFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "userinfo";
        });

        CompletableFuture<String> bookFuture = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return "bookInfo";
        });

        Thread.sleep(200);
        System.out.println("mainProcess");
        Thread.sleep(200);
        System.out.println(userFuture.get());
        System.out.println(bookFuture.get());
        System.out.println(System.currentTimeMillis() - startTime);
    }

    public static void main(String[] args) throws ExecutionException, InterruptedException {
//        testFuture();
        testFutureASync();
        testCompletableFuture();
    }
}
