package uw.studybuddy.UserProfile;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.CursorJoiner;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import uw.studybuddy.CourseInfo;
import uw.studybuddy.R;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link UserProfileFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link UserProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class UserProfileFragment extends Fragment {

    private TextView mUserQuestId;
    private EditText mUserDisplayName;
    private EditText mUserAboutMe;
    private UserInfo user;

    private Button mUserEditButton;
    private Button mAddCourseButton;

    private ImageButton mImage;
    private static final int GALLERY_REQUEST = 1;
    private Uri mImageUri;// = null;

    private LinearLayout mUserCoursesLayout;
    private List<Button> mCoursesButtons;
    private List<CourseInfo> mCoursesList;

    private StorageReference mStorage;
    private DatabaseReference mDatabase;

    private String questId;
  

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String TAG = "user_profile_fragment";
    private OnFragmentInteractionListener mListener;

    public UserProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment UserProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static UserProfileFragment newInstance(String param1, String param2) {
        UserProfileFragment fragment = new UserProfileFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


        Setup_UsertableListener();
        /*mAuthListener = new FirebaseAuth.AuthStateListener(){
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                //User = firebaseAuth.getCurrentUser();
                if(User != null){
                    //user log in state is fine
                    //do nothing for now
                    //mDatabase = FirebaseDatabase.getInstance().getReference();
                }else{
                    //user has already log out
                    //do the log in again
                    //dont know how to swicth back to log out
                    //To do
                    return;
                }
            }
        };
        */

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_user_profile, container, false);

        // Get display components from the view
        mUserQuestId = (TextView)rootView.findViewById(R.id.user_profile_quest_id);
        mUserDisplayName = (EditText)rootView.findViewById(R.id.user_profile_display_name);
        mUserCoursesLayout = (LinearLayout)rootView.findViewById(R.id.user_profile_courses_linear_layout);
        mUserAboutMe = (EditText)rootView.findViewById(R.id.user_profile_about_me);

        // Get edit buttons from the view
        mUserEditButton = (Button)rootView.findViewById(R.id.user_profile_edit_button);
        mAddCourseButton = (Button)rootView.findViewById(R.id.add_course_button);

        mStorage = FirebaseStorage.getInstance().getReference().child("Profile_image");
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        mImage = (ImageButton) rootView.findViewById(R.id.user_profile_image);

        // For the purpose of the demo, we will create a user to display
        user = UserInfo.getInstance();

        // Update the user profile view with the right user info
        mUserQuestId.setText(user.getQuestID());
        questId = user.getQuestID();
        mUserDisplayName.setText(user.getDisplayName());
        mUserAboutMe.setText(user.getAboutMe());
        
        //Toast.makeText(getActivity(), user.getAboutMe(), Toast.LENGTH_LONG).show();
        /*UserPattern Userholder = new UserPattern();

        Userholder.get_user(UserInfo.getInstance().getmUserTable_DS(), user.getDisplayName());
        String temp_uri = Userholder.getImage();*/
        String temp_uri = user.getmUserTable_DS().child(questId).child("image").getValue(String.class);
        if(temp_uri != null){
            mImageUri = Uri.parse(temp_uri);
            if(mImageUri != null) {
                //Toast.makeText(getActivity(), mImageUri.toString(), Toast.LENGTH_LONG).show();
                Picasso.with(getContext()).load(mImageUri).into(mImage);
            }
        } else {
            mImageUri = Uri.parse("android.resource://uw.studybuddy/mipmap/ic_default_user");
            UserInfo.setmImage(mImageUri);
            Picasso.with(getContext()).load(mImageUri).into(mImage);
        }

        // Add the courses buttons
        mCoursesList = UserInfo.getInstance().getCoursesList();
        int numCourses = mCoursesList.size();
        mCoursesButtons = new ArrayList<>();
        mUserCoursesLayout.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        int diameter = 200;
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(diameter, diameter-10);
        params.setMargins(2,2,2,2);

        mImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent galleryIntent = new Intent();
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        for (int i=0; i<numCourses; i++) {
            final Button button = createButton(mCoursesList.get(i));
            mCoursesButtons.add(button);
            final int  index = i;
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showDialog(index, button, false);
                }
            });
            mUserCoursesLayout.addView(button,params);
        }

        setListeners();

        // Should update current user profile with names
        //User = FirebaseAuth.getInstance().getCurrentUser();
        mUserAboutMe.setText(user.getAboutMe());

        return rootView;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == GALLERY_REQUEST && resultCode == getActivity().RESULT_OK){

            //Uri imageUri = data.getData();

            CropImage.activity().setAspectRatio(1, 1).start(getContext(), this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == getActivity().RESULT_OK) {
                mImageUri = result.getUri();
                mImage.setImageURI(mImageUri);
                StorageReference filepath = mStorage.child(mImageUri.getLastPathSegment());
                filepath.putFile(mImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    // if image upload successfully
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        @SuppressWarnings("VisibleForTests") Uri cur = taskSnapshot.getDownloadUrl();
                        String downloadUri = cur.toString();
                        user.setmImage(cur);
                        mDatabase.child(questId).child("image").setValue(downloadUri);
                    }
                });
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
            }
        }
    }

    private Button createButton(CourseInfo course) {
        Button button = new Button(this.getContext());
        button.setBackgroundResource(R.drawable.rounded_corners_button);
        button.setText(course.toString());
        button.setTextSize(17);
        button.setClickable(true);
        button.setGravity(Gravity.CENTER_VERTICAL | Gravity.CENTER_HORIZONTAL);
        return button;
    }

    public void Setup_UsertableListener(){
        FirebaseUserInfo.getUsersTable().addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                UserInfo.getInstance().setmUserTable_DS(dataSnapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    // Using the same dialog to add and edit courses
    private void showDialog(final int index, Button button, final boolean isAdd) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        View view = LayoutInflater.from(this.getContext()).inflate(R.layout.dialog_edit_course_name, null);
        final EditText edit_dialog_course_subject = (EditText) view.findViewById(R.id.edit_course_subject);
        final EditText edit_dialog_course_number = (EditText) view.findViewById(R.id.edit_course_number);
        builder.setView(view);

        final Button btn = button;
        final int i = index;

        if (isAdd) {
            builder.setTitle("Add New Course");
        } else {
            edit_dialog_course_subject.setText(mCoursesList.get(index).getSubject());
            edit_dialog_course_number.setText(mCoursesList.get(index).getCatalogNumber());

            builder.setTitle("Edit Course");
            builder.setNegativeButton("delete", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    user.deleteCourse(i);
                    reloadFragment();
                }
            });
        }

        builder.setNeutralButton("cancel",null);
        builder.setPositiveButton("confirm", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String sub = edit_dialog_course_subject.getText().toString().toUpperCase();
                String num = edit_dialog_course_number.getText().toString();
                if (isAdd) {
                    user.addCourse(sub, num);
                } else {
                    user.updateCourseInfo(i, sub, num);
//                    btn.setText(text);
                }
                reloadFragment();

            }
        });
        builder.show();
    }

    // Refresh fragment when user deletes a course
    private void reloadFragment() {
        getFragmentManager().beginTransaction().detach(this).attach(this).commit();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (user == null) {
            user = UserInfo.getInstance();
        }

        mUserDisplayName.setText(user.getDisplayName());
        mUserQuestId.setText(user.getQuestID());

        mUserAboutMe.setText(user.getAboutMe());
        int len = mCoursesList.size();
        for (int i=0; i<len; i++) {
            mCoursesButtons.get(i).setText(mCoursesList.get(i).toString());
        }
    }


    private void setListeners() {
        final EditText[] userInfoEditTexts = {mUserDisplayName, mUserAboutMe};
        for (EditText e: userInfoEditTexts) {
            e.setFocusable(false);
        }

        mUserEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Boolean editable = false;
                for (EditText e: userInfoEditTexts) {
                    if (e.isFocusable()) {
                        e.setFocusable(false);
                        reloadFragment();

                    } else {
                        editable = true;
                        e.setFocusableInTouchMode(true);

                    }
                }
                // Update image of edit button accordingly
                if (editable) {
                    mUserEditButton.setText("Done");
                    FirebaseUserInfo.update_name_list(mUserDisplayName.getText().toString());
                } else {
                    mUserEditButton.setText("Edit");
                    FirebaseUserInfo.update_name_list(mUserDisplayName.getText().toString());
                }

            }
        });

        mAddCourseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialog(0, null, true);
            }
        });

        for (int i=0; i<userInfoEditTexts.length; i++) {
            final EditText e = userInfoEditTexts[i];
            final int index = i;
            e.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {}
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (getTextFromUserProfile(index) != e.getText().toString()) {
                        setTextForUserProfile(index, e.getText().toString());
                    }
                }
            });
        }

    }



    private String getTextFromUserProfile(int index) {
        String answer;
        switch (index) {
            case 0: answer = user.getDisplayName(); break;
            case 1: answer = user.getAboutMe(); break;
            default: answer = "Error"; break;
        }
        Log.d(TAG, answer);
        return answer;
    }

    private void setTextForUserProfile(int index, String newText) {
        switch (index) {
            case 0: user.setDisplayName(newText); break;
            case 1: user.setAboutMe(newText); break;
            default: break;
        }
        Log.d(TAG, newText);
    }



    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
