// TitlePage.jsx
import React from "react";
import { useNavigate } from "react-router-dom";
import CenterWrapper from "../styles/CenterWrapper.jsx";
import logo from "../assets/SayToReserveLogo.png";

const TitlePage = () => {
  const navigate = useNavigate();

  const handleKakaoLogin = () => {
    // 백엔드 카카오 OAuth 시작 URL로 리디렉트
    window.location.href = "http://localhost:8080/oauth/kakao";
  };

  return (
    <CenterWrapper>
      {/* 콘텐츠 박스 */}
      <div className="text-center">
        <img src={logo} alt="SayToReserveLogo" className="mx-auto w-40 mb-4" />
        <h1 className="text-3xl font-bold mb-2 text-white">SayToReserve</h1>
        <p className="mb-6 text-white">환영합니다!</p>
        {/* 기본 로그인/회원가입 버튼 */}
        <div>
          <button
            onClick={() => navigate("/signup")}
            className="bg-blue-600 text-white px-4 py-2 rounded hover:bg-blue-700"
          >
            회원가입
          </button>
          <button
            onClick={() => navigate("/login")}
            className="ml-4 bg-gray-600 text-white px-4 py-2 rounded hover:bg-gray-700"
          >
            로그인
          </button>
        </div>
        {/* 구분선 */}
        <div className="text-white text-sm mt-2 mb-2">또는</div>
        {/* 카카오 소셜 로그인 버튼 */}
        <button
          onClick={handleKakaoLogin}
          className="bg-yellow-300 text-black font-bold px-4 py-2 rounded hover:bg-yellow-400"
        >
          {/*<img*/}
          {/*  src="https://static.kakao.com/static/img/common/logo/kakaolink_btn_small.png"*/}
          {/*  alt="Kakao"*/}
          {/*  className="inline-block w-5 h-5 mr-2 align-middle"*/}
          {/*/>*/}
          카카오 로그인
        </button>
        {/* 구분선 */}
        <div className="text-white text-sm mt-2 mb-2"></div>
        <button
          onClick={() =>
            (window.location.href = "http://localhost:8080/oauth/google")
          }
          className="bg-white text-black font-bold px-4 py-2 rounded hover:bg-gray-200 mt-2"
        >
          <img
            src="https://developers.google.com/identity/images/g-logo.png"
            alt="Google"
            className="inline-block w-5 h-5 mr-2 align-middle"
          />
          구글 로그인
        </button>
      </div>
    </CenterWrapper>
  );
};

export default TitlePage;
