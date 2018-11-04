package com.zhou;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

public class main {
    public static void main(String[] arg) {

//        a2b();
//        aAnda();
        observable();
    }

    private static void observable() {
        Observable observable =
                Observable.create(new Observable.OnSubscribe<String>() {
            @Override
            public void call(Subscriber<? super String> subscriber) {
                subscriber.onNext("100");
            }
        });
        Subscriber<String> subscriber = new Subscriber<String>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onNext(String s) {
                System.out.println(s);
            }
        };
        observable.subscribe(subscriber);

        observable.subscribe(new Action1<String>() {
            @Override
            public void call(String s) {
                System.out.println("second "+s);
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
