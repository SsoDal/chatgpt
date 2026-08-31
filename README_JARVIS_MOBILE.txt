JARVIS Mobile 0.6.0

목적
- Android 휴대폰에서 PC의 JARVIS_2 모바일 서버로 명령을 전송합니다.
- PC JARVIS API: GET /api/status, POST /api/command

사용
1. PC에서 JARVIS_2를 실행합니다.
2. PC JARVIS 모바일 탭에 표시되는 주소와 토큰을 확인합니다.
3. JARVIS Mobile APK를 휴대폰에 설치합니다.
4. 앱에 서버 주소와 토큰을 입력하고 저장합니다.
5. 연결 테스트가 PASS이면 명령 입력 또는 음성 명령을 사용합니다.

네트워크
- 같은 Wi-Fi/LAN에서 PC의 192.168.x.x 주소를 사용합니다.
- 외부에서는 공인 포트포워딩보다 Tailscale 같은 사설망 주소 사용을 권장합니다.

음성
- Android의 한국어 음성 인식(ko-KR)을 호출합니다.
- '음성 인식 후 바로 실행'이 체크되어 있으면 인식 직후 PC에 전송됩니다.
