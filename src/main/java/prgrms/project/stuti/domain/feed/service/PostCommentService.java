package prgrms.project.stuti.domain.feed.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import prgrms.project.stuti.domain.feed.model.PostComment;
import prgrms.project.stuti.domain.feed.model.Post;
import prgrms.project.stuti.domain.feed.repository.PostCommentRepository;
import prgrms.project.stuti.domain.feed.repository.PostRepository;
import prgrms.project.stuti.domain.feed.service.dto.PostCommentContentsResponse;
import prgrms.project.stuti.domain.feed.service.dto.PostCommentCreateDto;
import prgrms.project.stuti.domain.feed.service.dto.PostCommentGetDto;
import prgrms.project.stuti.domain.feed.service.dto.CommentParentContents;
import prgrms.project.stuti.domain.feed.service.dto.PostCommentResponse;
import prgrms.project.stuti.domain.feed.service.dto.PostCommentUpdateDto;
import prgrms.project.stuti.domain.member.model.Member;
import prgrms.project.stuti.domain.member.repository.MemberRepository;
import prgrms.project.stuti.global.error.exception.CommentException;
import prgrms.project.stuti.global.error.exception.MemberException;
import prgrms.project.stuti.global.error.exception.PostException;
import prgrms.project.stuti.global.page.offset.PageResponse;

@Service
@RequiredArgsConstructor
public class PostCommentService {

	private final PostCommentRepository postCommentRepository;
	private final PostRepository postRepository;
	private final MemberRepository memberRepository;

	@Transactional
	public PostCommentResponse createComment(PostCommentCreateDto postCommentCreateDto) {
		Post post = getPostById(postCommentCreateDto.postId());
		Member foundMember = getMemberById(postCommentCreateDto.memberId());
		PostComment parentPostComment = null;
		if (postCommentCreateDto.parentId() != null) {
			parentPostComment = getCommentById(postCommentCreateDto.parentId());
		}
		PostComment newPostComment = PostCommentConverter.toComment(postCommentCreateDto.contents(), post,
			parentPostComment, foundMember);
		PostComment savedPostComment = postCommentRepository.save(newPostComment);

		return PostCommentConverter.toCommentResponse(savedPostComment);
	}

	@Transactional
	public PostCommentResponse changeComment(PostCommentUpdateDto postCommentUpdateDto) {
		PostComment postComment = getCommentById(postCommentUpdateDto.postCommentId());
		validateEditMember(postComment, postCommentUpdateDto.memberId());
		if (postComment.getPost() == null) { //추후 isdelete로 변경시 로직확인 필요, 대댓글인 경우 댓글 있는지 확인 필요
			PostException.POST_NOT_FOUND();
		}
		postComment.changeContents(postCommentUpdateDto.contents());

		return PostCommentConverter.toCommentResponse(postComment);
	}

	@Transactional
	public void deleteComment(Long postId, Long commentId, Long memberId) {
		validatePostById(postId);
		PostComment foundPostComment = getCommentById(commentId);
		validateEditMember(foundPostComment, memberId);

		deleteComments(foundPostComment);
	}

	@Transactional(readOnly = true)
	public PageResponse<CommentParentContents> getPostComments(PostCommentGetDto postCommentGetDto) {
		getPostById(postCommentGetDto.postId());

		return postCommentRepository.findAllByPostIdAndParentIdIsNUllWithNoOffset(postCommentGetDto.postId(),
			postCommentGetDto.lastCommentId(), postCommentGetDto.size());
	}

	@Transactional(readOnly = true)
	public PostCommentContentsResponse getCommentContents(Long postId, Long commentId) {
		validatePostById(postId);
		PostComment postComment = getCommentById(commentId);

		return PostCommentConverter.toCommentContentsResponse(postComment);
	}

	private void deleteComments(PostComment deletePostComment) {
		if (deletePostComment.getParent() == null) {
			postCommentRepository.deleteAllByParentId(deletePostComment.getId());
		}
		postCommentRepository.delete(deletePostComment);
	}

	private PostComment getCommentById(Long parentCommentId) {
		return postCommentRepository.findById(parentCommentId)
			.orElseThrow(() -> CommentException.PARENT_COMMENT_NOT_FOUND(parentCommentId));
	}

	private Post getPostById(Long postId) {
		return postRepository.findById(postId).orElseThrow(PostException::POST_NOT_FOUND);
	}

	private Member getMemberById(Long memberId) {
		return memberRepository.findById(memberId).orElseThrow(() -> MemberException.notFoundMember(memberId));
	}

	private void validatePostById(Long postId) {
		postRepository.findById(postId).orElseThrow(PostException::POST_NOT_FOUND);
	}

	private void validateEditMember(PostComment comment, Long editMemberId) {
		Member commentEditor = getMemberById(editMemberId);
		Member commentCreator = comment.getMember();
		if (!commentEditor.getId().equals(commentCreator.getId())) {
			PostException.INVALID_EDITOR();
		}
	}
}
