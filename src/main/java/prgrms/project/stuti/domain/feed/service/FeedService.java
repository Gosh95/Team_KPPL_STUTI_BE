package prgrms.project.stuti.domain.feed.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import prgrms.project.stuti.domain.feed.controller.dto.RegisterPostRequest;
import prgrms.project.stuti.domain.feed.model.Feed;
import prgrms.project.stuti.domain.feed.model.FeedImage;
import prgrms.project.stuti.domain.feed.repository.FeedImageRepository;
import prgrms.project.stuti.domain.feed.repository.FeedRepository;
import prgrms.project.stuti.domain.feed.service.dto.PostChangeDto;
import prgrms.project.stuti.domain.feed.service.dto.PostCreateDto;
import prgrms.project.stuti.domain.feed.service.dto.PostDto;
import prgrms.project.stuti.domain.feed.service.dto.PostIdResponse;
import prgrms.project.stuti.domain.feed.service.dto.FeedResponse;
import prgrms.project.stuti.domain.member.model.Member;
import prgrms.project.stuti.domain.member.repository.MemberRepository;
import prgrms.project.stuti.global.error.exception.FeedException;
import prgrms.project.stuti.global.error.exception.NotFoundException;
import prgrms.project.stuti.global.uploader.LocalImageUploader;
import prgrms.project.stuti.global.uploader.common.ImageDirectory;

@Service
@RequiredArgsConstructor
public class FeedService {

	private final FeedRepository feedRepository;
	private final MemberRepository memberRepository;
	private final LocalImageUploader localImageUploader;
	private final FeedImageRepository feedImageRepository;

	@Transactional
	public PostIdResponse registerPost(PostCreateDto postDto) {
		Optional<Member> findMember = memberRepository.findById(postDto.memberId());
		if (findMember.isEmpty()) {
			NotFoundException.MEMBER_NOT_FOUND.get();
		}
		Feed feed = FeedConverter.toPost(postDto, findMember.get());
		Feed savedFeed = feedRepository.save(feed);

		String uploadUrl = localImageUploader.upload(postDto.imageFile(), ImageDirectory.FEED);
		FeedImage feedImage = new FeedImage(uploadUrl, savedFeed);
		feedImageRepository.save(feedImage);

		return FeedConverter.toPostIdResponse(savedFeed.getId());
	}

	@Transactional(readOnly = true)
	public FeedResponse getAllPosts(Long lastPostId, int size) {
		List<PostDto> postsDtos = feedRepository.findAllWithNoOffset(lastPostId, size);
		boolean hasNext = hasNext(lastPostId);

		return FeedConverter.toFeedResponse(postsDtos, hasNext);
	}

	@Transactional
	public PostIdResponse changePost(PostChangeDto postChangeDto) {
		Feed feed = feedRepository.findById(postChangeDto.postId()).orElseThrow(FeedException::FEED_NOT_FOUND);
		feed.changeContents(postChangeDto.contents());

		feedImageRepository.deleteByFeedId(feed.getId());
		if(postChangeDto.imageFile() != null) {
			String uploadUrl = localImageUploader.upload(postChangeDto.imageFile(), ImageDirectory.FEED);
			FeedImage feedImage = new FeedImage(uploadUrl, feed);
			feedImageRepository.save(feedImage);
		}

		return FeedConverter.toPostIdResponse(feed.getId());
	}

	private boolean hasNext(Long lastPostId) {
		if (lastPostId == null) {
			return feedRepository.existsByIdGreaterThanEqual(1L);
		}
		return feedRepository.existsByIdLessThan(lastPostId);
	}
}
