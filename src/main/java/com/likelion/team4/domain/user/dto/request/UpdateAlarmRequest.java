package com.likelion.team4.domain.user.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateAlarmRequest {

    @NotNull(message = "루틴 체크 알림 켜기/끄기 설정은 필수입니다.")
    private Boolean routineAlarmOn;

    @NotNull(message = "대체미션 리마인드 켜기/끄기 설정은 필수입니다.")
    private Boolean altMissionReminderOn;

    @NotNull(message = "알림 음조 설정은 필수입니다.")
    @Pattern(regexp = "^(차분한벨|신나는벨|무음)$", message = "알 수 없는 알림 음조 형식입니다.")
    private String alarmSound;

    @NotNull(message = "알림 시간 유형 설정은 필수입니다.")
    @Pattern(regexp = "^(1시간전|2시간전|직접설정)$", message = "알 수 없는 알림 시간 유형 형식입니다.")
    private String alarmOffsetType;

    // CUSTOM이 아닌 경우 null이 들어올 수 있으므로 NotNull을 제외합니다.
    @Min(value = 1, message = "알림 시간은 최소 1분 이상이어야 합니다.")
    @Max(value = 1440, message = "알림 시간은 최대 1440분(24시간)까지만 설정 가능합니다.")
    private Integer alarmOffsetMinutes;
}