package org.magnum.mobilecloud.video;

import java.io.IOException;
import java.security.Principal;
import java.util.Collection;

import javax.servlet.http.HttpServletResponse;

import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import retrofit.http.GET;
import retrofit.http.Path;

import com.google.common.collect.Lists;

/**
 * 
 * @author -Silvia
 *
 */
@Controller
public class VideoSvc {

	@Autowired
	private VideoRepository videos;
    
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
		return Lists.newArrayList(videos.findAll());
	}
	
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH, method = RequestMethod.POST)
	public @ResponseBody Video addVideo(@RequestBody Video v) {
		v.setLikes(0);
		videos.save(v);
		return v;
	}
	
	@RequestMapping(value = VideoSvcApi.VIDEO_TITLE_SEARCH_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Collection<Video> findByTitle(@RequestParam(VideoSvcApi.TITLE_PARAMETER) String title)
	{
		return Lists.newArrayList(videos.findByName(title));
	}	
	
	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/likedby", method = RequestMethod.GET)
	@ResponseBody
	public Collection<String> getUsersWhoLikedVideo(@PathVariable("id") long id,
			HttpServletResponse response) throws IOException
	{
		Video video = getVideo(id, response);
		
		return video.getUsersWhoLike();
	}	
	
	@RequestMapping(value = VideoSvcApi.VIDEO_DURATION_SEARCH_PATH, method = RequestMethod.GET)
	@ResponseBody
	public Collection<Video> findByDurationLessThan(@RequestParam(VideoSvcApi.DURATION_PARAMETER) long duration)
	{
		return Lists.newArrayList(videos.findByDurationLessThan(duration));
	}		

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/like", method = RequestMethod.POST)
	public @ResponseBody void likeVideo(@PathVariable("id") long id, Principal principal,
			HttpServletResponse response) throws IOException {
		
		Video video = getVideo(id, response);
		
		if(response.isCommitted()){
			return;
		} else if(video.getUsersWhoLike().contains(principal.getName())) {
			response.sendError(400);
			return;
		} else {
			video.setLikes(video.getLikes() + 1);
			video.getUsersWhoLike().add(principal.getName());
			videos.save(video);
		}
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}/unlike", method = RequestMethod.POST)
	public void unlikeVideo(@PathVariable("id") long id, Principal principal,
			HttpServletResponse response) throws IOException {
		
		Video video = getVideo(id, response);
		
		if(response.isCommitted()){
			return;
		} else if(!video.getUsersWhoLike().contains(principal.getName())) {
			response.sendError(400);
			return;
		} else {
			video.setLikes(video.getLikes() - 1);
			video.getUsersWhoLike().remove(principal.getName());
			videos.save(video);
		}
	
	}

	@RequestMapping(value = VideoSvcApi.VIDEO_SVC_PATH + "/{id}", method = RequestMethod.GET)
	public @ResponseBody Video getVideo(@PathVariable("id") Long id, HttpServletResponse response) throws IOException
    {
		Video v = null;
		try
		{
			v = videos.findOne(id);
			if(v == null)
			{
				response.sendError(404);
			}
			else
			{
				return v;
			}
		}
		catch (Exception e)
		{
			response.sendError(404);
			throw e;
		}
		return v;
    }
	
}