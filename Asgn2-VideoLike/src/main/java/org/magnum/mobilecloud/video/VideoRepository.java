package org.magnum.mobilecloud.video;

import java.util.Collection;

import org.magnum.mobilecloud.video.client.VideoSvcApi;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * An interface for a repository that can store Video
 * objects and allow them to be searched by title.
 * 
 * @author jules
 *
 */
@Repository
public interface VideoRepository extends CrudRepository<Video, Long>{

	public Collection<Video> findByName(@Param(VideoSvcApi.TITLE_PARAMETER)String title);

	public Collection<Video> findByDurationLessThan(long duration);
	
}
