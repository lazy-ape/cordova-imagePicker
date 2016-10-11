/**
 * An Image Picker Plugin for Cordova/PhoneGap.
 */
package com.synconset;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;

import org.apache.cordova.LOG;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.Permission;
import java.util.ArrayList;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

public class ImagePicker extends CordovaPlugin {
    public static String TAG = "ImagePicker";
    private static final int CHECK_PERMISSIONS_REQ_CODE = 1;
    private static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;

    private CallbackContext permissionCallback;
    private CallbackContext callbackContext;
    private JSONObject params;

    public boolean execute(String action, final JSONArray args, final CallbackContext callbackContext) throws JSONException {


        this.callbackContext = callbackContext;
        this.params = args.getJSONObject(0);
        if (action.equals("getPictures")) {
            if (cordova.hasPermission(WRITE_EXTERNAL_STORAGE)) {
                getPictures();
            } else {
                permissionCallback = callbackContext;
                cordova.requestPermission(this, CHECK_PERMISSIONS_REQ_CODE, WRITE_EXTERNAL_STORAGE);
            }
        }
        return true;
    }


    private void getPictures() throws JSONException {
        Intent intent = new Intent(cordova.getActivity(), MultiImageChooserActivity.class);
        int max = 20;
        int desiredWidth = 0;
        int desiredHeight = 0;
        int quality = 100;
        if (this.params.has("maximumImagesCount")) {
            max = this.params.getInt("maximumImagesCount");
        }
        if (this.params.has("width")) {
            desiredWidth = this.params.getInt("width");
        }
        if (this.params.has("height")) {
            desiredHeight = this.params.getInt("height");
        }
        if (this.params.has("quality")) {
            quality = this.params.getInt("quality");
        }
        intent.putExtra("MAX_IMAGES", max);
        intent.putExtra("WIDTH", desiredWidth);
        intent.putExtra("HEIGHT", desiredHeight);
        intent.putExtra("QUALITY", quality);
        if (this.cordova != null) {
            this.cordova.startActivityForResult((CordovaPlugin) this, intent, 0);
        }
    }

    @Override
    public void onRequestPermissionResult(int requestCode, String[] permissions,
                                          int[] grantResults) throws JSONException {

        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                LOG.d(TAG, "User *rejected* location permission");
                this.permissionCallback.error("Location permission is required to discover unpaired devices.");
                return;
            }
        }

        switch (requestCode) {
            case CHECK_PERMISSIONS_REQ_CODE:
                LOG.d(TAG, "User granted location permission");
                getPictures();
                break;
        }
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && data != null) {
            ArrayList<String> fileNames = data.getStringArrayListExtra("MULTIPLEFILENAMES");
            JSONArray res = new JSONArray(fileNames);
            this.callbackContext.success(res);
        } else if (resultCode == Activity.RESULT_CANCELED && data != null) {
            String error = data.getStringExtra("ERRORMESSAGE");
            this.callbackContext.error(error);
        } else if (resultCode == Activity.RESULT_CANCELED) {
            JSONArray res = new JSONArray();
            this.callbackContext.success(res);
        } else {
            this.callbackContext.error("No images selected");
        }
    }
}
