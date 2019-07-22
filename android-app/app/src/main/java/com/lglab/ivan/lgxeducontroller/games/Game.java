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
        if (imageName.startsWith("1234_")) {
            // load image
            try {
                // get input stream
                InputStream ims = context.getAssets().open(imageName.substring(5));
                // load image as Drawable
                return BitmapFactory.decodeStream(ims);
            } catch (IOException ex) {
                return null;
            }
        }

        return !imageName.equals("") ? BitmapFactory.decodeFile(new File(context.getFilesDir().toString() + "/saved_images", imageName).getAbsolutePath()) : null;
    }

    public void setImage(File file) {
        this.imageName = file.getName();
    }

    public void setNewImage(Bitmap bitmap, Context context) {
        imageName = generateRandomString();

        if (bitmap == null) {
            Bitmap.Config conf = Bitmap.Config.ARGB_8888;
            bitmap = Bitmap.createBitmap(300, 300, conf);
        }

        final Bitmap bitmap1 = bitmap;

        new Thread(() -> {
            String root = context.getFilesDir().toString();
            File myDir = new File(root + "/saved_images");
            if (!myDir.exists()) {
                myDir.mkdirs();
            }

            File file = new File(myDir, imageName);
            if (file.exists())
                file.delete();
            try {
                FileOutputStream out = new FileOutputStream(file);
                bitmap1.compress(Bitmap.CompressFormat.JPEG, 50, out);
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
        final int COMPRESSION_QUALITY = 100;
        String encodedImage;
        try {
            ByteArrayOutputStream byteArrayBitmapStream = new ByteArrayOutputStream();
            bitmapPicture = Bitmap.createScaledBitmap(bitmapPicture, 300, 300, true);
            bitmapPicture.compress(Bitmap.CompressFormat.PNG, COMPRESSION_QUALITY,
                    byteArrayBitmapStream);
            byte[] b = byteArrayBitmapStream.toByteArray();
            encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
            return encodedImage;
        } catch (Exception e) {
            Log.d("Game", e.getMessage());
            return "";
        }
    }

    private static Bitmap getBitmapFromString(String stringPicture) {
        try {
            byte[] decodedString = Base64.decode(stringPicture, Base64.DEFAULT);
            Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);
            return decodedByte;
        } catch (Exception e) {
            Log.d("Game", e.getMessage());
            return null;
        }
    }

    private static String generateRandomString() {

        int leftLimit = 97; // letter 'a'
        int rightLimit = 122; // letter 'z'
        int targetStringLength = 30;

        StringBuilder buffer = new StringBuilder(targetStringLength);
        for (int i = 0; i < targetStringLength; i++) {
            int randomLimitedInt = leftLimit + (int)
                    (random.nextFloat() * (rightLimit - leftLimit + 1));
            buffer.append((char) randomLimitedInt);
        }
        return buffer.toString();
    }
}
