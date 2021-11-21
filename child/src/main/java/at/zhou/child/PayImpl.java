package at.zhou.child;

import android.util.Log;

import com.zhou.ipay.IPay;

import java.util.Random;

/**
 * Created by mxz on 2021/9/10.
 */
public class PayImpl implements IPay {

    private Random random = new Random();

    @Override
    public int getMoney() {
        return random.nextInt(5000);
    }

    @Override
    public void pay(int money) {
        Log.i("child", "PayImpl pay money: " + money);
    }
}
