package com.zhou;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

public class main {
    public static void main(String[] arg) {

//        a2b();
        aAnda();
    }

    private static void a2b() {
        Random r = new Random();
        List<Integer> numbers = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            numbers.add(r.nextInt(101));
        }
        Observable.from(numbers)
                .map(new Func1<Integer, Boolean>() {

                    @Override
                    public Boolean call(Integer integer) {
                        System.out.print(integer);
                        return integer % 2 == 0;
                    }
                })
                .subscribe(new Subscriber<Boolean>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Boolean aBoolean) {
                        System.out.println("  result: " + aBoolean);
                    }
                });
    }

    private static void aAnda() {
        List<User> data = new ArrayList<>();
        String[] source = {"class_a", "class_b", "class_c", "class_d"};
        String[] teachers = {"teacher_a", "teacher_b", "teacher_c", "teacher_d"};
        List<Source> list = new ArrayList<>();
        Random r = new Random();
        for (int i = 0; i < 6; i++) {
            int size = r.nextInt(10);
            list.clear();
            for (int j = 0; j < size; j++) {
                list.add(new Source(source[r.nextInt(4)], teachers[r.nextInt(4)]));
            }
            data.add(new User("zhang", r.nextInt(40), list));
        }
        Observable.from(data)
                .flatMap(new Func1<User, Observable<Source>>() {
                    @Override
                    public Observable<Source> call(User user) {
                        return Observable.from(user.sources);
                    }
                })
                .subscribe(new Subscriber<Source>() {
                    @Override
                    public void onCompleted() {
                        System.out.println("Done");
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Source source) {
                        System.out.print(source.teacher + " -->" + source.className);
                        System.out.print(" ,");
                    }
                });
    }
}
