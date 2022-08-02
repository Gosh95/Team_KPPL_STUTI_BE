package prgrms.project.stuti.domain.studygroup.repository.studymember;

import static prgrms.project.stuti.domain.member.model.QMember.*;
import static prgrms.project.stuti.domain.studygroup.model.QStudyGroup.*;
import static prgrms.project.stuti.domain.studygroup.model.QStudyMember.*;
import static prgrms.project.stuti.domain.studygroup.repository.CommonStudyGroupBooleanExpression.*;

import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;
import prgrms.project.stuti.domain.studygroup.model.StudyMemberRole;

@RequiredArgsConstructor
public class CustomStudyMemberRepositoryImpl implements CustomStudyMemberRepository {

	private final JPAQueryFactory jpaQueryFactory;

	@Override
	public boolean isLeader(Long memberId, Long studyGroupId) {
		Integer result = jpaQueryFactory
			.selectOne()
			.from(studyMember)
			.join(studyMember.member, member)
			.join(studyMember.studyGroup, studyGroup)
			.where(hasStudyMemberRole(StudyMemberRole.LEADER), isEqualIdAndNotDeletedMember(memberId),
				isEqualIdAndNotDeletedStudyGroup(studyGroupId))
			.fetchFirst();

		return result != null;
	}
}
