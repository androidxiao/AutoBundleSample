package com.black.autobundlesample;

import android.content.Intent;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.black.autobundlesample.model.Person;
import com.black.autobundlesample.model.Student;
import com.black.lib_annotation.AutoBundle;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;


public class SecondActivity extends AppCompatActivity {

    public static final String TAG = "ez";

    @AutoBundle
    public int id;
    @AutoBundle
    public String name;
    @AutoBundle
    public boolean is;
    @AutoBundle
    public byte mByte;
    @AutoBundle
    public char b;
    @AutoBundle
    public short mShort;
    @AutoBundle
    public long mLong;
    @AutoBundle
    public float mFloat;
    @AutoBundle
    public Double mDouble;
    @AutoBundle
    public CharSequence mCharSequence;
    @AutoBundle
    public Parcelable mParcelable;
    @AutoBundle
    public Parcelable[] mParcelableArray;
    @AutoBundle(isParcelableArrayList = true)
    public ArrayList<Person> mParcelableList;
    @AutoBundle
    public ArrayList<Integer> mParcelableIntegerList;
    @AutoBundle
    public ArrayList<String> mParcelableStringList;
    @AutoBundle
    public String[] mArray;
    @AutoBundle
    public ArrayList<CharSequence> mCharSequencesArrayList;
    @AutoBundle(isSerializable = true)
    public Student mStudent;
    @AutoBundle(isSerializable = true)
    public List<Student> mStudents;
    @AutoBundle(isParcelable = true)
    public Person mPerson;
    @AutoBundle(isParcelableArray = true)
    public Person[] mPeople;
    @AutoBundle
    public boolean[] mBooleans;
    @AutoBundle
    public byte[] mBytes;
    @AutoBundle
    public short[] mShorts;
    @AutoBundle
    public char[] mChars;
    @AutoBundle
    public int[] mInts;
    @AutoBundle
    public long[] mLongs;
    @AutoBundle
    public double[] mDoubles;
    @AutoBundle
    public CharSequence[] mCharSequences;
    @AutoBundle
    public Bundle mBundle;
    @AutoBundle(isBundle = true)
    public String mBundleString;
    @AutoBundle(isBundle = true)
    public boolean mBundleBoolean;
    @AutoBundle(isBundle = true)
    public byte mBundleByte;
    @AutoBundle(isBundle = true)
    public char mBundleChar;
    @AutoBundle(isBundle = true)
    public short mBundleShort;
    @AutoBundle(isBundle = true)
    public int mBundleInt;
    @AutoBundle(isBundle = true)
    public long mBundleLong;
    @AutoBundle(isBundle = true)
    public float mBundleFloat;
    @AutoBundle(isBundle = true)
    public float mBundleDouble;
    @AutoBundle(isBundle = true)
    public CharSequence mBundleCharSequence;
    @AutoBundle(isParcelable = true,isBundle = true)
    public Person mBundlePerson;
    @AutoBundle(isParcelableArray = true,isBundle = true)
    public Person[] mBundlePersonArray;
    @AutoBundle(isParcelableArrayList = true,isBundle = true)
    public ArrayList<Person> mBundleParcelableList;
    @AutoBundle(isBundle = true)
    public ArrayList<Integer> mBundleIntegerList;
    @AutoBundle(isBundle = true)
    public ArrayList<String> mBundleStringList;
    @AutoBundle(isBundle = true)
    public ArrayList<CharSequence> mBundleCharSequencesArrayList;
    @AutoBundle(isSerializable = true,isBundle = true)
    public Student mBundleStudent;
    @AutoBundle(isBundle = true)
    public boolean[] mBundleBooleans;
    @AutoBundle(isBundle = true)
    public byte[] mBundleBytes;
    @AutoBundle(isBundle = true)
    public short[] mBundleShorts;
    @AutoBundle(isBundle = true)
    public char[] mBundleChars;
    @AutoBundle(isBundle = true)
    public int[] mBundleInts;
    @AutoBundle(isBundle = true)
    public long[] mBundleLongs;
    @AutoBundle(isBundle = true)
    public float[] mBundleFloats;
    @AutoBundle(isBundle = true)
    public double[] mBundleDoubles;
    @AutoBundle(isBundle = true)
    public String[] mBundleStrings;
    @AutoBundle(isBundle = true)
    public CharSequence[] mBundleCharSequences;
    @AutoBundle(isCloseFromActivity = true,exclude = true)
    public boolean closeMainActivity;
    @AutoBundle(isCloseFromActivity = true,exclude = true)
    public boolean closeMainActivity1;
    @AutoBundle(addFlags = true,exclude = true)
    public int mSecondActivityLaunchMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_second);
        TextView mTvTest1 = findViewById(R.id.tv_test1);

        Intent intent = new Intent();
        intent.putExtra("", "");
        mShort = intent.getShortExtra("", (short) 0);
        intent.getByteExtra("", (byte) 0);
        char extra = intent.getCharExtra("", (char) 0);
        float extra1 = intent.getFloatExtra("", 0);
        double extra2 = intent.getDoubleExtra("", 0);
        CharSequence extra3 = intent.getCharSequenceExtra("");
        Person extra4 = (Person)intent.getParcelableExtra("");
        mParcelableArray = getIntent().getParcelableArrayExtra("aa");
        ArrayList<Parcelable> extra6 = intent.getParcelableArrayListExtra("aa");
        ArrayList<Integer> extra7 = intent.getIntegerArrayListExtra("");
        ArrayList<String> extra9 = intent.getStringArrayListExtra("");
        ArrayList<CharSequence> extra10 = intent.getCharSequenceArrayListExtra("");
//        Student extra11 = (Student) intent.getSerializableExtra("");
        Bundle bb = getIntent().getBundleExtra("bb");
//        String cc = bb.getString("cc");
//        Log.d(TAG, "onCreate: cc-->"+cc);
        mTvTest1.setText("我是第二个页面");


        new SecondActivityAutoBundle().bindIntent(this, getIntent());


        mTvTest1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent1 = new Intent();
                setResult(RESULT_OK, intent1);
                finish();
            }
        });

        Log.d(TAG, "onCreate: mBundleString--->"+mBundleString+" mBundleBoolean--->"+mBundleBoolean+"  mBundleByte-->"+mBundleByte+" mBundleChar--->"+mBundleChar+"\n  mBundleShort--->"+mBundleShort+"  mBundleInt-->"+mBundleInt+"  mBundleLong-->"+mBundleLong+"  mBundleFloat--->"+mBundleFloat+"  mBundleDouble--->"+mBundleDouble+"  mBundleCharSequence-->"+mBundleCharSequence+" mBundlePerson-->"+mBundlePerson.getName()+"  mBundlePersonArray-->"+mBundlePersonArray[0].getName()+"\n  mBundleParcelableList-->"+mBundleParcelableList.get(0).getName()+" mBundleIntegerList-->"+mBundleIntegerList.get(0)+"  mBundleStringList-->"+mBundleStringList.get(0)+" mBundleCharSequencesArrayList-->"+mBundleCharSequencesArrayList.get(0)+"  mBundleStudent-->"+mBundleStudent.getAddress()+"\n  mBundleBooleans-->"+mBundleBooleans[0]+" mBundleBytes-->"+mBundleBytes[1]+" mBundleShorts-->"+mBundleShorts[0]+" mBundleChars-->"+mBundleChars[1]+"\n  mBundleInts-->"+mBundleInts[1]
        +" mBundleLongs-->"+mBundleLongs[1]+"  mBundleFloats-->"+mBundleFloats[1]+"  mBundleDoubles-->"+mBundleDoubles[1]+" mBundleStrings-->"+mBundleStrings[1]+" mBundleCharSequences-->"+mBundleCharSequences[1]);
        Log.d(TAG, "onCreate: id-->" + id + "  name-->" + name + "  is--->" + is+" person-->"+mPerson.getName()+" student-->"+mStudent.getName()+" "+mStudent.getAddress());
        Log.d(TAG, "onCreate: mBoolean[]-->"+mBooleans[0]+" "+mBooleans[1]+" byte-->"+mBytes[0]+" "+mBytes[1]+" mShorts--->"+mShorts[0]+" "+mShorts[1]+" mChars-->"+mChars[0]+" "+mChars[1]);
        for (int i = 0; i < mPeople.length; i++) {
            Log.d(TAG, "onCreate: mPeople---> "+mPeople[i].getName()+" "+mPeople[i].getAge());
        }
        for (int i = 0; i < mParcelableList.size(); i++) {
            Log.d(TAG, "onCreate: mParcelableList---> "+mParcelableList.get(i).getName()+" "+mParcelableList.get(i).getAge());
        }

    }
}
