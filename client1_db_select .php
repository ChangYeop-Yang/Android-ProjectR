<?php
	/* MYSQL 설정 */
	$connect = mysqli_connect("localhost", "root", "ckdduq2580!", "ProjectR"); /* Mysql 접속 설정 */

	mysqli_query("SET NAMES utf-8"); /* 인코딩 설정 */
	
	$Port = $_GET["port"]; /* 구역 */
	$Date = $_GET["date"]; /* 날짜 */
	$Team = $_GET["team"]; /* 팀 */

	$Query = "SELECT * FROM GameClient WHERE Port='$Port' AND Date='$Date' AND Team='$Team'"; /* DB Table을 조회하는 쿼리문 변수 생성 */

	$result = mysqli_query($connect, $Query); /* Query문 실행 및 변수 저장 */
	
	$total_number = mysqli_num_rows($result); /* 현재 DB에 저장된 총 데이터 수 조회 */

	/* XML 파일 만들어주는 구문 */
	$xml_result = "<?xml version = '1.0' encoding = 'utf-8'?>"; /* XML 처음 부분 저장 */
	$xml_result .= "<client>"; /* XML 시작 부분 */

	/* 데이터를 저장하는 구문 */
	while($cut = mysqli_fetch_assoc($result))
	{		
		$xml_result .="<item>";
		$xml_result .="<mapX>$cut[Latitude]</mapX>"; /* 이름 */
		$xml_result .="<mapY>$cut[Longitude]</mapY>"; /* 대화 */
		$xml_result .="<name>$cut[Name]</name>"; /* 대화 */
		$xml_result .="</item>";
	}
	$xml_result .= "<total_rows>$total_number</total_rows>"; /* 총 갯수 */
	$xml_result .= "</client>"; /* XML 끝 부분 */
	
	header('Content-type: text/xml'); /* XML Type 설정 */
	echo $xml_result;

	mysqli_close($connect); /* Mysql 종료 */
?>