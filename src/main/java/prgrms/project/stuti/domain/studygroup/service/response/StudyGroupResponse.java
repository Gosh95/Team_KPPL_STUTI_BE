package prgrms.project.stuti.domain.studygroup.service.response;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.Builder;
import prgrms.project.stuti.domain.member.model.Mbti;

public record StudyGroupResponse(
	Long studyGroupId,
	Long memberId,
	String thumbnailUrl,
	String topic,
	String title,
	Set<Mbti> preferredMBTIs,
	String region,
	LocalDateTime startDateTime,
	LocalDateTime endDateTime,
	int numberOfMembers,
	int numberOfRecruits
) {

	@Builder
	public StudyGroupResponse {
	}
}