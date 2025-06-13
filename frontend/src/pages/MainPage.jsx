// src/pages/MainPage.jsx
import React from "react";
import CenterWrapper from "../styles/CenterWrapper.jsx";

const MainPage = () => {
  return (
    <CenterWrapper>
      <div className="p-8 text-center">
        <h1 className="text-3xl font-bold mb-4">메인 페이지</h1>
        <p className="text-lg">로그인 완료 메인 기능 페이지</p>
      </div>
    </CenterWrapper>
  );
};

export default MainPage;
