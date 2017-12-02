package in.com.emma;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.AppSettingsDialog;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity {

    public static final int RC_STORAGE = 123;
    public static final int BASE_INDEX = 0;
    public static final int PICTURE_REQUEST_CODE = 1002;
    @BindView(R.id.btn_photo)
    Button btnPhoto;

    private Uri mOutputFileUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

    }


    @AfterPermissionGranted(RC_STORAGE)
    public void openImageIntent(View v) {
        List<String> perms = new ArrayList<>();
        perms.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        if (EasyPermissions.hasPermissions(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
            // Determine Uri of camera image to save.
            final File root = ImageUtils.getRootFolder(this);
            final String fName;
            try {
                fName = ImageUtils.createImageFile();

                final File sdImageMainDirectory = new File(root, fName);
                mOutputFileUri = Uri.fromFile(sdImageMainDirectory);

                // Camera.
                final List<Intent> cameraIntents = new ArrayList<>();
                final Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                final PackageManager packageManager = getPackageManager();
                final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, BASE_INDEX);
                for (ResolveInfo res : listCam) {
                    final String packageName = res.activityInfo.packageName;
                    final Intent intent = new Intent(captureIntent);
                    intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                    intent.setPackage(packageName);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, mOutputFileUri);
                    cameraIntents.add(intent);
                }

                // Filesystem.
                final Intent galleryIntent = new Intent(Intent.ACTION_PICK);
                galleryIntent.setType("image/*");

                // Chooser of filesystem options.
                final Intent chooserIntent = Intent.createChooser(galleryIntent, getString(R.string.select_source));

                // Add the camera options.
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));

                startActivityForResult(chooserIntent, PICTURE_REQUEST_CODE);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            EasyPermissions.requestPermissions(this, getString(R.string.dialog_storage),
                    RC_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            if (requestCode == PICTURE_REQUEST_CODE) {
                final boolean isCamera;
                if (data == null) {
                    isCamera = true;
                } else {
                    final String action = data.getAction();
                    if (action == null) {
                        isCamera = false;
                    } else {
                        isCamera = action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
                    }
                }


                Uri mSelectedImageUri;
                if (isCamera) {
                    mSelectedImageUri = mOutputFileUri;
                } else if (data.getData() != null) {
                    mSelectedImageUri = data.getData();
                } else {
                    mSelectedImageUri = mOutputFileUri;
                }
                if (mSelectedImageUri != null) {
                    try {
                        setImage(mSelectedImageUri);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

    }

    public void setImage(final Uri imageUri) throws IOException {

    }
}
