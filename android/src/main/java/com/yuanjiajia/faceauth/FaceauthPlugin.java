package com.yuanjiajia.faceauth;

import android.content.Context;
import android.text.TextUtils;
import android.widget.Toast;

import com.tencent.faceid.FaceIdClient;
import com.tencent.faceid.auth.CredentialProvider;
import com.tencent.faceid.exception.ClientException;
import com.tencent.faceid.exception.ServerException;
import com.tencent.faceid.model.GetLipLanguageRequest;
import com.tencent.faceid.model.GetLipLanguageResult;
import com.tencent.faceid.model.ImageIdCardCompareRequest;
import com.tencent.faceid.model.ImageIdCardCompareResult;
import com.tencent.faceid.model.VideoImageIdentityRequest;
import com.tencent.faceid.model.VideoImageIdentityResult;

import io.flutter.plugin.common.MethodCall;
import io.flutter.plugin.common.MethodChannel;
import io.flutter.plugin.common.MethodChannel.MethodCallHandler;
import io.flutter.plugin.common.MethodChannel.Result;
import io.flutter.plugin.common.PluginRegistry.Registrar;

/** FaceauthPlugin */
public class FaceauthPlugin implements MethodCallHandler {
    interface Callback<T>{
        void onSucceed(T result);
        void onFailed();
    }
    private Context context;
    private FaceIdClient client;
    private int mCurrentRequestId;
    private CredentialProvider provider;
    public FaceauthPlugin(Context context){
        this.context = context;
    }
    /** Plugin registration. */
    public static void registerWith(Registrar registrar) {
    //    String appid = "1257084581"; // 您申请到的 APPID
    //    FaceIdClient mFaceIdClient = new FaceIdClient(registrar.context(), appid);
    final MethodChannel channel = new MethodChannel(registrar.messenger(), "faceauth");
        channel.setMethodCallHandler(new FaceauthPlugin(registrar.context()));
    }

    private void initSdk(String appId){
        client = new FaceIdClient(context, appId);
    }

    /**
     * 获取唇语验证码，用于活体核身
     * @param bucketName bucket 名称
     * @param sign 鉴权签名, 测试时可以调用 {@link CredentialProvider#getMultipleSign(String, long)} 来生成
     * @param seq 请求标识，用于日志查询
     */
    private void getLipCaptcha(final String bucketName, final String sign, final String seq,final Callback<GetLipLanguageResult> callback) {

        final GetLipLanguageRequest request = new GetLipLanguageRequest(bucketName, seq);
        request.setSign(sign);
        mCurrentRequestId = request.getRequestId();

        new Thread() {
            @Override
            public void run() {
                try {
                    GetLipLanguageResult result = client.getLipLanguage(request);
                    System.out.println(result);
                    if(result != null){
                        callback.onSucceed(result);
                    }
                } catch (ClientException e) {
                    e.printStackTrace();
                    callback.onFailed();
                } catch (ServerException e) {
                    e.printStackTrace();
                    callback.onFailed();
                }
            }
        }.start();
    }

    /**
     * 活体核身（通过视频和照片）
     * <ol>
     *     <li>视频活体检测</li>
     *     <li>视频与照片相似度检测</li>
     * </ol>
     * @param lip 唇语验证码
     * @param videoPath 视频文件路径
     * @param imagePath 人脸图片文件路径
     * @param seq 请求标识，用于日志查询
     * @param sign 鉴权签名, 测试时可以调用 {@link CredentialProvider#getMultipleSign(String, long)} 来生成
     * @param bucketName bucket 名称
     */
    private void videoAuth(String lip, String videoPath, String imagePath, String seq, String sign, String bucketName) {
        final VideoImageIdentityRequest request;
        if (TextUtils.isEmpty(imagePath)) {
            request = new VideoImageIdentityRequest(bucketName, lip, videoPath, seq);
        } else {
            request = new VideoImageIdentityRequest(bucketName, lip, videoPath, imagePath, true, seq);
        }
        request.setSign(sign);
        mCurrentRequestId = request.getRequestId();

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    VideoImageIdentityResult result = client.videoImageIdentity(request);
                    System.out.println(result);
                } catch (ClientException e) {
                    e.printStackTrace();
                } catch (ServerException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    @Override
    public void onMethodCall(MethodCall call, final Result result) {
        System.out.println("call method:"+call.method+","+call.arguments);
        if (call.method.equals("getPlatformVersion")) {
            result.success("Android " + android.os.Build.VERSION.RELEASE);
        }else if(call.method.equals("initSdk")){
            if(call.hasArgument("appId")
                    && call.hasArgument("secretId")
                    && call.hasArgument("secretKey")){
                String appId = call.argument("appId");
                String secretId = call.argument("secretId");
                String secretKey = call.argument("secretKey");
                initSdk(appId);
                provider = new CredentialProvider(appId,secretId,secretKey);
                result.success(true);
            }else{
                //TODO
                result.error("ERROR","missing argument",null);
            }
        }else if(call.method.equals("getLipCaptcha")){
            if(call.hasArgument("bucket")){
                String bucket = call.argument("bucket");
                String sign = provider.getMultipleSign(bucket,1000);
                getLipCaptcha(bucket, sign, null, new Callback<GetLipLanguageResult>() {
                    @Override
                    public void onSucceed(GetLipLanguageResult getLipLanguageResult) {
                        result.success(getLipLanguageResult.getValidateData());
                    }

                    @Override
                    public void onFailed() {

                    }
                });
            }else{
                result.error("ERROR","missing argument",null);
            }

        }else if(call.method.equals("videoAuth")){

        }else {
            result.notImplemented();
        }
    }
}
