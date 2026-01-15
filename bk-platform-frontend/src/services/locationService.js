import axiosClient from "../api/axiosClient";

export const locationService = {
    getAllProvinces: () => {
        return axiosClient.get('/locations/provinces');
    },
    getDistrictsByProvince: (provinceCode) => {
        // SỬA LẠI: Thêm dấu `
        return axiosClient.get(`/locations/districts/${provinceCode}`);
    },
    getWardsByDistrict: (districtCode) => {
        // SỬA LẠI: Thêm dấu `
        return axiosClient.get(`/locations/wards/${districtCode}`);
    }
};