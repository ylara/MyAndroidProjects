package com.kz.domin;

public class SortModel {

	private String name;   //��ʾ������
	private String sortLetters;  //��ʾ����ƴ��������ĸ
	private long id; // ����ID 3
	private String title; // �������� 0
	private String album; // ר�� 7
	private long albumId;//ר��ID 6
	private String displayName; //��ʾ���� 4
	private String artist; // �������� 2
	private long duration; // ����ʱ�� 1
	private long size; // ������С 8
	private String url; // ����·�� 5
	private String path;
	private String lrcTitle; // �������
	private String lrcSize; // ��ʴ�С 
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public void setPath(String path){
		this.path = path;
	}
	public String getPath(){
		return path;
	}
	public long getDuration(){
		return duration;
	}
	public void setDuration(long duration) {
		this.duration = duration;
	}
	public String getSortLetters() {
		return sortLetters;
	}
	public void setSortLetters(String sortLetters) {
		this.sortLetters = sortLetters;
	}
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getAlbum() {
		return album;
	}

	public void setAlbum(String album) {
		this.album = album;
	}

	

	public long getAlbumId() {
		return albumId;
	}

	public void setAlbumId(long albumId) {
		this.albumId = albumId;
	}

	public String getArtist() {
		return artist;
	}

	public void setArtist(String artist) {
		this.artist = artist;
	}

	public long getSize() {
		return size;
	}

	public void setSize(long size) {
		this.size = size;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getLrcTitle() {
		return lrcTitle;
	}

	public void setLrcTitle(String lrcTitle) {
		this.lrcTitle = lrcTitle;
	}

	public String getLrcSize() {
		return lrcSize;
	}

	public void setLrcSize(String lrcSize) {
		this.lrcSize = lrcSize;
	}

	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}
	
	

}
