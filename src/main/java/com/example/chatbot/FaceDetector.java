package com.example.chatbot;

import org.bytedeco.javacpp.Loader;

import java.io.File;
import java.io.IOException;

import static org.bytedeco.javacpp.opencv_core.*;
import static org.bytedeco.javacpp.opencv_imgcodecs.cvLoadImage;
import static org.bytedeco.javacpp.opencv_objdetect.*;

public final class FaceDetector {
    private static final String HAAR_CASCADE_FILE =
            "haarcascade_frontalface_alt.xml";

    private final CvHaarClassifierCascade mClassifier;
    private final CvMemStorage mStorage;

    public FaceDetector() throws IOException {
        final File classifierFile = Loader.extractResource(
                HAAR_CASCADE_FILE, null, "classifier", ".xml");
        if (classifierFile == null || classifierFile.length() <= 0) {
            throw new IOException("Could not extract \"" + classifierFile + "\" from Java resources.");
        }

        mClassifier = new CvHaarClassifierCascade(cvLoad(classifierFile.getAbsolutePath()));
        classifierFile.delete();
        if (mClassifier.isNull()) {
            throw new IOException("Could not load the classifier file.");
        }

        mStorage = CvMemStorage.create();
    }

    public int numFacesInImage(String imagePath) {
        final IplImage image = cvLoadImage(imagePath);
        final CvSeq faces = cvHaarDetectObjects(
                image,
                mClassifier,
                mStorage,
                1.1,
                3,
                CV_HAAR_DO_CANNY_PRUNING
        );

        cvClearMemStorage(mStorage);

        return faces.total();
    }
}
