import React from "react";
import { useNavigate } from "react-router-dom";

const MainPage = () => {
  const navigate = useNavigate();

  return (
    <div style={{ textAlign: "center", marginTop: "5px" }}>
      <h1>SayToReserve</h1>
      <p>환영합니다!</p>
      <button onClick={() => navigate("/signup")}>회원가입</button>
      <button onClick={() => navigate("/login")} style={{ marginLeft: "10px" }}>
        로그인
      </button>
    </div>
  );
};

export default MainPage;
