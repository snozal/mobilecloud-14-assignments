package org.magnum.dataup;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.magnum.dataup.model.Video;
import org.magnum.dataup.model.VideoStatus;
import org.magnum.dataup.model.VideoStatus.VideoState;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import retrofit.http.Part;
import retrofit.http.Path;
import retrofit.http.Streaming;

/**
 * 
 * @author -Silvia
 *
 */
@Controller
public class VideoController {

	VideoFileManager videoDataMgr;
	
	public Collection<Video> videoCollection;

    private static final AtomicLong currentId = new AtomicLong(0L);

    private Map<Long,Video> videos = new HashMap<Long, Video>();
	/**
	 * This endpoint in the API returns a list of the videos that have
	 * been added to the server. The Video objects should be returned as
	 * JSON. 
	 * 
	 * To manually test this endpoint, run your server and open this URL in a browser:
	 * http://localhost:8080/video
	 * 
	 * @return
	 */
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Collection<Video> getVideoList()
	{
		return videos.values();
	}
	
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v) {
		
		checkAndSetId(v);
		
		v.setDataUrl(getDataUrl(v.getId()));
		
		save(v);
		
		return v;
	}
	
	@RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.POST)
	public @ResponseBody VideoStatus setVideoData(@PathVariable(VideoSvcApi.ID_PARAMETER) long id,
			@RequestParam(VideoSvcApi.DATA_PARAMETER) MultipartFile videoData,
			HttpServletResponse response) throws IOException  
	{
		Video v = videos.get(id);
		try {
			if (v == null) {
				response.sendError(404);
				return null;
			} else {
				if(videoData != null){
					saveSomeVideo(v, videoData);
				}
			}
		} catch (IOException e) {
			response.sendError(404);
			return null;
		}
		return new VideoStatus(VideoState.READY);
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_DATA_PATH, method = RequestMethod.GET)
	void getData(@PathVariable(VideoSvcApi.ID_PARAMETER) Long id, HttpServletResponse response) throws IOException
    {
		Video v = null;
		try
		{
			v = videos.get(id);
		}
		catch (Exception e)
		{
			response.sendError(404);
			throw e;
		}
    }

    private String getDataUrl(Long videoId){
        String url = getUrlBaseForLocalServer() + "/video/" + videoId + "/data";
        return url;
    }

    private String getUrlBaseForLocalServer() {
       HttpServletRequest request = 
           ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
       String base = 
          "http://"+request.getServerName() 
          + ((request.getServerPort() != 80) ? ":"+request.getServerPort() : "");
       return base;
    }

    public Video save(Video entity) {
        checkAndSetId(entity);
        videos.put(entity.getId(), entity);
        return entity;
    }

    private void checkAndSetId(Video entity) {
        if(entity.getId() == 0){
            entity.setId(currentId.incrementAndGet());
        }
    }

    public void saveSomeVideo(Video v, MultipartFile videoData) throws IOException {
    	setVideoData();
        videoDataMgr.saveVideoData(v, videoData.getInputStream());
   }


    public void serveSomeVideo(Video v, HttpServletResponse response) throws IOException {
         // Of course, you would need to send some headers, etc. to the client too!
    	setVideoData();
        videoDataMgr.copyVideoData(v, response.getOutputStream());
    }

    public void setVideoData () {
                try {
                videoDataMgr = VideoFileManager.get();
                } catch (IOException e) {
                e.printStackTrace();
                }

    }
}