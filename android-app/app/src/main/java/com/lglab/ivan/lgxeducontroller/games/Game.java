package com.lglab.ivan.lgxeducontroller.games;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Base64;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public abstract class Game implements IJsonPacker, Parcelable {

    private static Random random = new Random();

    private long id;
    private String name;
    private String category;
    private String imageName;
    private GameEnum type;
    private String fileId;
    private List<Question> questions;

    public Game() {
        id = 0;
        name = "";
        category = "";
        imageName = "";
        type = null;
        fileId = "";
        questions = new ArrayList<>();
    }

    public Game(Parcel in) {
        this();
        id = in.readLong();
        name = in.readString();
        category = in.readString();
        imageName = in.readString();
        type = GameEnum.findByName(in.readString());
        fileId = in.readString();
        questions = in.readArrayList(createQuestion().getClass().getClassLoader());
    }

    @Override
    public Game unpack(JSONObject obj) throws JSONException {
        name = obj.getString("name");
        category = obj.getString("category");
        imageName = obj.getString("imageName");
        type = GameEnum.findByName(obj.getString("type"));
        if (type == null)
            throw new JSONException("No game type found!");

        unpackQuestions(obj.getJSONArray("questions"));
        return this;
    }

    public Game unpack_external(JSONObject obj, Context context) throws JSONException {
        name = obj.getString("name");
        category = obj.getString("category");
        setNewImage(getBitmapFromString(obj.getString("image")), context);

        type = GameEnum.findByName(obj.getString("type"));
        if (type == null)
            throw new JSONException("No game type found!");

        unpackQuestions(obj.getJSONArray("questions"));
        return this;
    }

    @Override
    public JSONObject pack() throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("name", name);
        obj.put("category", category);
        obj.put("imageName", imageName);
        obj.put("type", type.name());

        JSONArray array = new JSONArray();
        for (int i = 0; i < questions.size(); i++) {
            array.put(questions.get(i).pack());
        }
        obj.put("questions", array);

        return obj;
    }

    public JSONObject pack_external(Context context) throws JSONException {
        JSONObject obj = new JSONObject();

        obj.put("name", name);
        obj.put("category", category);
        obj.put("image", getStringFromBitmap(getImage(context)));
        obj.put("type", type.name());

        JSONArray array = new JSONArray();
        for (int i = 0; i < questions.size(); i++) {
            array.put(questions.get(i).pack());
        }
        obj.put("questions", array);

        return obj;
    }

    private void unpackQuestions(JSONArray array) throws JSONException {
        for (int i = 0; i < array.length(); i++) {
            Question q = createQuestion();
            if (q != null)
                questions.add(q.unpack(array.getJSONObject(i)));
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeLong(id);
        parcel.writeString(name);
        parcel.writeString(category);
        parcel.writeString(imageName);
        parcel.writeString(type.name());
        parcel.writeString(fileId);
        parcel.writeList(questions);
    }

    public String getNameForExporting() {
        return name.replaceAll("[:/*\"?|<> ]", "_") + ".json";
    }

    public abstract Question createQuestion();

    public abstract GameManager createManager();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Bitmap getImage(Context context) {
        if (imageName.startsWith("games/")) {
            // load image
            try {
                // get input stream
                InputStream ims = context.getAssets().open(imageName);
                // load image as Drawable
                return BitmapFactory.decodeStream(ims);
            } catch (IOException ex) {
                return null;
            }
        }

        Bitmap.Config conf = Bitmap.Config.ARGB_8888;
        return !imageName.equals("") ? BitmapFactory.decodeFile(new File(context.getFilesDir().toString() + "/saved_images", imageName).getAbsolutePath()) :  Bitmap.createBitmap(300, 300, conf);
    }

    public void setImage(File file) {
        this.imageName = file.getName();
    }

    public void setNewImage(Bitmap bitmap, Context context) {
        imageName = UUID.randomUUID().toString();

        if (bitmap == null) {
            bitmap = Bitmap.createBitmap(300, 300, Bitmap.Config.ARGB_8888);
        }

        final Bitmap bitmap1 = getScaledBitmap(bitmap);

        new Thread(() -> {
            String root = context.getFilesDir().toString();
            File myDir = new File(root + "/saved_images");
            if (!myDir.exists()) {
                if(myDir.mkdirs()) {
                    Log.d("Game", "OOPS!");
                }
            }

            File file = new File(myDir, imageName);
            if (file.exists()) {
                if(!file.delete()) {
                    Log.d("Game", "OOPS!");
                }
            }
            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap1.compress(Bitmap.CompressFormat.JPEG, 100, out);
                out.flush();
                out.close();

            } catch (Exception e) {
                e.printStackTrace();
            }

            Log.i("image", "image saved to >>>" + file.getAbsolutePath());
        }).start();
    }

    public GameEnum getType() {
        return type;
    }

    public void setType(GameEnum type) {
        this.type = type;
    }

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    private static String getStringFromBitmap(Bitmap bitmapPicture) {
        String encodedImage;
        try {
            ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
            bitmapPicture = getScaledBitmap(bitmapPicture);
            bitmapPicture.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayBitmapStream);
            byte[] b = byteArrayBitmapStream.toByteArray();
            encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
            return encodedImage;
        } catch (Exception e) {
            Log.d("Game", e.toString());
            return "";
        }
    }

    private static Bitmap getBitmapFromString(String stringPicture) {
        try {
            byte[] decodedString = Base64.decode(stringPicture, Base64.DEFAULT);
            return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
        } catch (Exception e) {
            Log.d("Game", e.toString());
            return null;
        }
    }

    private static Bitmap getScaledBitmap(Bitmap bitmap) {
        final int maxSize = 300;

        int outWidth;
        int outHeight;
        int inWidth = bitmap.getWidth();
        int inHeight = bitmap.getHeight();
        if(inWidth > inHeight){
            outWidth = maxSize;
            outHeight = (inHeight * maxSize) / inWidth;
        } else {
            outHeight = maxSize;
            outWidth = (inWidth * maxSize) / inHeight;
        }

        return Bitmap.createScaledBitmap(bitmap, outWidth, outHeight, false);
    }
}
