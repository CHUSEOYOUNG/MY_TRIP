package com.sist.web.dao;

import java.util.List;

import com.sist.web.model.Region;

public interface RegionDao {
    void insertRegion(Region region);
    List<Region> getAllRegions();  // 필요에 따라 사용
    //지역코드조회 추가
    public Region regionSelect(String regionName);
}