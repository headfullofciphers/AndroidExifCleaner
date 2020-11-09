package com.example.exif;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.content.Context;

import androidx.annotation.RequiresApi;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class ExifUtils
{
    private static final String[] exifAttributes = new String[] {
            "FNumber", "ApertureValue", "Artist", "BitsPerSample", "BrightnessValue", "CFAPattern",
            "ColorSpace", "ComponentsConfiguration", "CompressedBitsPerPixel", "Compression",
            "Contrast", "Copyright", "CustomRendered", "DateTime", "DateTimeDigitized",
            "DateTimeOriginal", "DefaultCropSize", "DeviceSettingDescription","DigitalZoomRatio",
            "DNGVersion", "ExifVersion", "ExposureBiasValue", "ExposureIndex", "ExposureMode",
            "ExposureProgram", "ExposureTime", "FileSource", "Flash", "FlashpixVersion",
            "FlashEnergy", "FocalLength", "FocalLengthIn35mmFilm", "FocalPlaneResolutionUnit",
            "FocalPlaneXResolution", "FocalPlaneYResolution", "FNumber", "GainControl",
            "GPSAltitude", "GPSAltitudeRef", "GPSAreaInformation", "GPSDateStamp", "GPSDestBearing",
            "GPSDestBearingRef", "GPSDestDistance", "GPSDestDistanceRef", "GPSDestLatitude",
            "GPSDestLatitudeRef", "GPSDestLongitude", "GPSDestLongitudeRef", "GPSDifferential",
            "GPSDOP", "GPSImgDirection", "GPSImgDirectionRef", "GPSLatitude", "GPSLatitudeRef",
            "GPSLongitude", "GPSLongitudeRef", "GPSMapDatum", "GPSMeasureMode",
            "GPSProcessingMethod", "GPSSatellites", "GPSSpeed", "GPSSpeedRef", "GPSStatus",
            "GPSTimeStamp", "GPSTrack", "GPSTrackRef", "GPSVersionID", "ImageDescription",
            "ImageLength", "ImageUniqueID", "ImageWidth", "InteroperabilityIndex",
            "ISOSpeedRatings", "ISOSpeedRatings", "JPEGInterchangeFormat",
            "JPEGInterchangeFormatLength", "LightSource", "Make", "MakerNote", "MaxApertureValue",
            "MeteringMode", "Model", "NewSubfileType", "OECF", "AspectFrame", "PreviewImageLength",
            "PreviewImageStart", "ThumbnailImage", "Orientation", "PhotometricInterpretation",
            "PixelXDimension", "PixelYDimension", "PlanarConfiguration", "PrimaryChromaticities",
            "ReferenceBlackWhite", "RelatedSoundFile", "ResolutionUnit", "RowsPerStrip", "ISO",
            "JpgFromRaw", "SensorBottomBorder", "SensorLeftBorder", "SensorRightBorder",
            "SensorTopBorder", "SamplesPerPixel", "Saturation", "SceneCaptureType", "SceneType",
            "SensingMethod", "Sharpness", "ShutterSpeedValue", "Software",
            "SpatialFrequencyResponse", "SpectralSensitivity", "StripByteCounts", "StripOffsets",
            "SubfileType", "SubjectArea", "SubjectDistance", "SubjectDistanceRange",
            "SubjectLocation", "SubSecTime", "SubSecTimeDigitized", "SubSecTimeDigitized",
            "SubSecTimeOriginal", "SubSecTimeOriginal", "ThumbnailImageLength",
            "ThumbnailImageWidth", "TransferFunction", "UserComment", "WhiteBalance",
            "WhitePoint", "XResolution", "YCbCrCoefficients", "YCbCrPositioning",
            "YCbCrSubSampling", "YResolution"
    };

    private static final String[] exifAttributesToClean = new String[] {
            "Copyright", "CustomRendered", "DateTime", "DateTimeDigitized", "DateTimeOriginal",
            "ExifVersion", "ExposureBiasValue", "ExposureMode",  "ExposureProgram", "ExposureTime",
            "ExposureIndex", "FileSource", "FNumber", "GPSAltitude", "GPSAltitudeRef",
            "GPSAreaInformation", "GPSDateStamp", "GPSDestBearing", "GPSDestBearingRef",
            "GPSDestDistance", "GPSDestDistanceRef", "GPSDestLatitude", "GPSDestLatitudeRef",
            "GPSDestLongitude", "GPSDestLongitudeRef", "GPSDifferential", "GPSDOP",
            "GPSImgDirection", "GPSImgDirectionRef", "GPSLatitude", "GPSLatitudeRef",
            "GPSLongitude", "GPSLongitudeRef", "GPSMapDatum", "GPSMeasureMode",
            "GPSProcessingMethod", "GPSSatellites", "GPSSpeed", "GPSSpeedRef", "GPSStatus",
            "GPSTimeStamp", "GPSTrack", "GPSTrackRef", "GPSVersionID", "ImageDescription",
            "ImageUniqueID", "Make", "MakerNote", "Model","ISO",
    };

    public Context mContext;
    ExifInterface exif;



    public String getPathFromURI(Uri contentUri) {
        String res = null;
        String[] proj = {MediaStore.Images.Media.DATA};
        Cursor cursor = mContext.getContentResolver().query(contentUri, proj, null,
                null, null);
        if (cursor.moveToFirst()) {
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            res = cursor.getString(column_index);
        }
        cursor.close();
        return res;
    }


    public String getExifFromUri(Uri contentUri){
        String path = getPathFromURI(contentUri);
        StringBuilder attributes = new StringBuilder();
        Path source= Paths.get(path);
        attributes.append("parent: "+source.getParent().toString()+ "\n");
        attributes.append(path+"\n\n");
        try {
            exif = new ExifInterface(path);
            for(String attribute : exifAttributesToClean){
                attributes.append(attribute+": "+exif.getAttribute(attribute)+"\n");
            }
        } catch (IOException e) {
            attributes.append("\nUnable to load ExIF \n"+e.toString());
        }
        return  attributes.toString();
    }
    public Bitmap getThumbnail(Uri contentUri) throws IOException {
        exif = new ExifInterface(getPathFromURI(contentUri));
        return exif.getThumbnailBitmap();
    }


    public String getExtension(String fileName) {
        String extension = "";

        int i = fileName.lastIndexOf('.');
        if (i > 0) {
            extension = fileName.substring(i+1);
        }
        return "."+extension;
    }

    public void clearFile(String filePath)  throws IOException{
        exif = new ExifInterface(filePath);
        for(String attribute : exifAttributes){ // or exifAttributesToClean
            exif.setAttribute(attribute, "");
        }
        exif.saveAttributes();
    }

}
