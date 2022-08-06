package prgrms.project.stuti.domain.feed.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import prgrms.project.stuti.domain.feed.service.dto.CommentUpdateDto;
import prgrms.project.stuti.domain.feed.model.Comment;
import prgrms.project.stuti.domain.feed.model.Feed;
import prgrms.project.stuti.domain.feed.repository.CommentRepository;
import prgrms.project.stuti.domain.feed.repository.FeedRepository;
import prgrms.project.stuti.domain.feed.service.dto.CommentCreateDto;
import prgrms.project.stuti.domain.feed.service.dto.CommentResponse;
import prgrms.project.stuti.global.error.exception.CommentException;
import prgrms.project.stuti.global.error.exception.FeedException;

@Service
@RequiredArgsConstructor
public class CommentService {

	private final CommentRepository commentRepository;
	private final FeedRepository feedRepository;

	@Transactional
	public CommentResponse createComment(CommentCreateDto commentCreateDto) {
		Feed feed = feedRepository.findById(commentCreateDto.postId()).orElseThrow(FeedException::FEED_NOT_FOUND);
		Comment parentComment = null;
		if (commentCreateDto.parentId() != null) {
			parentComment = getParentComment(commentCreateDto.parentId());
		}
		Comment newComment = CommentConverter.toComment(commentCreateDto.contents(), feed, parentComment);
		Comment savedComment = commentRepository.save(newComment);

		return CommentConverter.toCommentResponse(savedComment);
	}

	@Transactional
	public CommentResponse changeComment(CommentUpdateDto commentUpdateDto) {
		Comment comment = commentRepository.findById(commentUpdateDto.postCommentId())
			.orElseThrow(() -> CommentException.COMMENT_NOT_FOUND(commentUpdateDto.postCommentId()));
		if(comment.getFeed() == null) { //추후 isdelete로 변경시 로직확인 필요, 대댓글인 경우 댓글 있는지 확인 필요
			FeedException.FEED_NOT_FOUND();
		}
		comment.changeContents(commentUpdateDto.contents());

		return CommentConverter.toCommentResponse(comment);
	}

	@Transactional
	public void deleteComment(Long postId, Long commentId, Long memberId) {
		feedRepository.findById(postId).orElseThrow(FeedException::FEED_NOT_FOUND);
		commentRepository.findById(commentId).orElseThrow(() -> CommentException.COMMENT_NOT_FOUND(commentId));
		commentRepository.deleteById(commentId);
	}

	private Comment getParentComment(Long parentCommentId) {
		return commentRepository.findById(parentCommentId)
			.orElseThrow(() -> CommentException.PARENT_COMMENT_NOT_FOUND(parentCommentId));
	}
}