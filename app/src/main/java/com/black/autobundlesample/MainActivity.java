package com.black.autobundlesample;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.black.autobundlesample.model.Person;
import com.black.autobundlesample.model.Student;
import com.black.autobundlesample.test.ThirdActivityAutoBundle;

import java.util.ArrayList;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    public static final String TAG = "ez";



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView mTvMsg1 = findViewById(R.id.tv_msg1);
        TextView mTvMsg2 = findViewById(R.id.tv_msg2);
        TextView mTvMsg3 = findViewById(R.id.tv_msg3);

        mTvMsg1.setText("我是测试");

        mTvMsg2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SecondActivityAutoBundle()
                        .closeMainActivity(true)
                        .mSecondActivityLaunchMode(Intent.FLAG_ACTIVITY_NEW_TASK)
                        .start(MainActivity.this);
            }
        });

        mTvMsg3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new SecondActivityAutoBundle()
                        .closeMainActivity1(false)
                        .start(MainActivity.this);
            }
        });
        
        final Person person1 = new Person("嗯嗯",1);
        final Person person2 = new Person("啊啊",2);
        final Person person3 = new Person("嘿嘿",3);

        final Person[] personArray = new Person[2];
        personArray[0] = person2;
        personArray[1] = person3;
        
        final ArrayList<Person> personList = new ArrayList<>();
        personList.add(person1);
        personList.add(person2);

        final ArrayList<Integer> bundleArrayListInt = new ArrayList<>();
        bundleArrayListInt.add(100);

        final Student student = new Student("小明","大凤阳");
        final ArrayList<String> bundleArrayListString = new ArrayList<>();
        bundleArrayListString.add("我是测试");
        final ArrayList<CharSequence> bundleArrayListCharSequence = new ArrayList<>();
        bundleArrayListCharSequence.add("p");
        final Student student1 = new Student("小红","滁州");
        final boolean[] bb = new boolean[2];
        bb[0]=true;
        bb[1]=true;
        final byte[] bytes = new byte[2];
        bytes[0]='o';
        bytes[1]='k';
        final short[] shorts = new short[2];
        shorts[0]=101;
        shorts[1]=102;
        final char[] chars = new char[2];
        chars[0]='p';
        chars[1]='h';
        final int[] ints = new int[2];
        ints[0]=199;
        ints[1]=109;
        final long[] longs = new long[2];
        longs[0]=200;
        longs[1]=201;
        final float[] floats = new float[2];
        floats[0]=300f;
        floats[1]=301f;
        final double[] doubles = new double[2];
        doubles[0] = 400;
        doubles[1] = 401;
        final String[] strings = new String[2];
        strings[0] = "abc";
        strings[1] = "xyz";
        final CharSequence[] charSequences = new CharSequence[2];
        charSequences[0] = "CharSequence000";
        charSequences[1] = "CharSequence555";
        mTvMsg1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new SecondActivityAutoBundle()
                        .id(1)
                        .is(true)
                        .name("from MainActivity")
                        .mPerson(new Person("aa", 10))
                        .mPeople(new Person[]{person1, person2})
                        .mParcelableList(personList)
                        .mStudent(student)
                        .mChars(new char[]{'a', 'b'})
                        .mShorts(new short[]{1, 2})
                        .mBytes(new byte[]{'b', 'c'})
                        .mBooleans(new boolean[]{true, true})
                        .mBundleString("好的")
                        .mBundleBoolean(true)
                        .mBundleByte((byte) 1)
                        .mBundleChar('z')
                        .mBundleShort((short) 20)
                        .mBundleInt(30)
                        .mBundleLong(40)
                        .mBundleFloat(50f)
                        .mBundleDouble(60)
                        .mBundleCharSequence("xyz")
                        .mBundlePerson(person3)
                        .mBundlePersonArray(personArray)
                        .mBundleParcelableList(personList)
                        .mBundleIntegerList(bundleArrayListInt)
                        .mBundleStringList(bundleArrayListString)
                        .mBundleCharSequencesArrayList(bundleArrayListCharSequence)
                        .mBundleStudent(student1)
                        .mBundleBooleans(bb)
                        .mBundleBytes(bytes)
                        .mBundleShorts(shorts)
                        .mBundleChars(chars)
                        .mBundleInts(ints)
                        .mBundleLongs(longs)
                        .mBundleFloats(floats)
                        .mBundleDoubles(doubles)
                        .mBundleStrings(strings)
                        .mBundleCharSequences(charSequences)
                        .startForResult(MainActivity.this, 10);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10) {
            Log.d(TAG, "onActivityResult: 我从 Secondactivity 返回了。。。");
        } else if (requestCode == 20) {

            Log.d(TAG, "onActivityResult: 我从 ThirdActivity 返回了。。。");
        }
    }
}
