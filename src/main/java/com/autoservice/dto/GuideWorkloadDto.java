package com.autoservice.dto;

public class GuideWorkloadDto {
    private final Long guideId;
    private final String guideName;
    private final long openTours;
    private final long inProgressTours;
    private final long completedTours;
    private final long totalTours;

    public GuideWorkloadDto(Long guideId,
                            String guideName,
                            long openTours,
                            long inProgressTours,
                            long completedTours,
                            long totalTours) {
        this.guideId = guideId;
        this.guideName = guideName;
        this.openTours = openTours;
        this.inProgressTours = inProgressTours;
        this.completedTours = completedTours;
        this.totalTours = totalTours;
    }

    public Long getGuideId() {
        return guideId;
    }

    public String getGuideName() {
        return guideName;
    }

    public long getOpenTours() {
        return openTours;
    }

    public long getInProgressTours() {
        return inProgressTours;
    }

    public long getCompletedTours() {
        return completedTours;
    }

    public long getTotalTours() {
        return totalTours;
    }
}
